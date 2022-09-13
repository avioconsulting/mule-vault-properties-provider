package com.avioconsulting.mule.vault.provider.api;

import com.avioconsulting.mule.vault.provider.internal.error.exception.SecretNotFoundException;
import com.avioconsulting.mule.vault.provider.internal.error.exception.UnsetVariableException;
import com.avioconsulting.mule.vault.provider.internal.error.exception.VaultAccessException;
import com.bettercloud.vault.Vault;
import com.bettercloud.vault.VaultException;
import org.mule.runtime.api.exception.DefaultMuleException;
import org.mule.runtime.config.api.dsl.model.properties.ConfigurationPropertiesProvider;
import org.mule.runtime.config.api.dsl.model.properties.ConfigurationProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Provider to read Vault properties from the Vault server.
 */
public class VaultConfigurationPropertiesProvider implements ConfigurationPropertiesProvider {

    private final static Logger logger = LoggerFactory.getLogger(VaultConfigurationPropertiesProvider.class);

    private final static String VAULT_PROPERTIES_PREFIX = "vault::";
    private final static Pattern VAULT_PATTERN = Pattern.compile(VAULT_PROPERTIES_PREFIX + "([^\\.]*)\\.([^}]*)");
    private final static Pattern ENV_PATTERN = Pattern.compile("\\$\\[([^\\]]*)\\]");

    private final Vault vault;

    private final boolean isLocalMode;

    Map<String, Map<String,String>> cachedData;

    /**
     * Constructs a VaultConfigurationPropertiesProvider. Vault must not be null.
     * 
     * If isLocalMode is true, it will use the 
     * 
     * @param vault
     */
    public VaultConfigurationPropertiesProvider(final Vault vault, final Boolean isLocalMode, final String localPropertiesFile) {
        this.vault = vault;
        cachedData = new HashMap<>();
        this.isLocalMode = isLocalMode;
        if (isLocalMode) {
            evaluateLocalProperitesConfig(localPropertiesFile);
        }
    }

    /**
     * Retrieves the property value from Vault. It stores the retrieved path in a Map so a Dynamic Secrets can be used.
     *
     * @param path     the path to the secret
     * @param property the property to retrieve from the secret
     * @return         the value of the property or null if the property is not found
     */
    private String getProperty(String path, String property) throws SecretNotFoundException, VaultAccessException, DefaultMuleException {
        try {
            Map<String, String> data = null;
            if (cachedData.containsKey(path)) {
                logger.trace("Getting data from cache");
                data = cachedData.get(path);
            } else if (!isLocalMode) {
                logger.trace("Getting data from Vault");
                data = vault.logical().read(path).getData();
                // TODO: Does the driver ever return null? Or does it throw an exception? It returns a null if there is no data stored in the secret
                cachedData.put(path, data);
            } else {
                // path is not in the cache and isLocalMode is true, so notify that the property is not in the local properties file
                throw new SecretNotFoundException(String.format("No property found for %s.%s in the local properties file", path, property));
            }

            if (data != null && data.containsKey(property)) {
                return data.get(property);
            } else {
                throw new SecretNotFoundException(String.format("No value found for %s.%s", path, property));
            }

        } catch (VaultException ve) {
            if (ve.getHttpStatusCode() == 404) {
                throw new SecretNotFoundException("Error getting data from Vault, secret not found", ve);
            } else if (ve.getHttpStatusCode() == 403) {
                throw new VaultAccessException(String.format("Access to the secret at \"%s\" is denied", path), ve);
            } else {
                logger.error("Error getting data from Vault", ve);
                throw new DefaultMuleException("Unknown Vault exception", ve);
            }
        }
    }

    /**
     * Get a configuration property value from Vault.
     *
     * @param configurationAttributeKey  the key to lookup
     * @return                           the String value of the property
     */
    @Override
    public Optional<ConfigurationProperty> getConfigurationProperty(String configurationAttributeKey) {

        if (configurationAttributeKey.startsWith(VAULT_PROPERTIES_PREFIX)) {
            Matcher matcher = VAULT_PATTERN.matcher(configurationAttributeKey);
            if (matcher.find()) {

                final String effectiveKey = configurationAttributeKey.substring(VAULT_PROPERTIES_PREFIX.length());

                // The Vault path is everything after the prefix and before the first period
                final String secretPath = matcher.group(1);

                // The key is everything after the first period
                final String key = matcher.group(2);

                try {
                    final String value = getProperty(expandedValue(secretPath), expandedValue(key));

                    if (value != null) {
                        return Optional.of(new ConfigurationProperty() {

                            @Override
                            public Object getSource() {
                                return "Vault provider source";
                            }

                            @Override
                            public Object getRawValue() {
                                return value;
                            }

                            @Override
                            public String getKey() {
                                return effectiveKey;
                            }
                        });
                    }
                } catch (Exception e) {
                    logger.error("Property was not found", e);
                    return Optional.empty();
                }

                return Optional.empty();

            }
        }
        return Optional.empty();
    }

    @Override
    public String getDescription() {
        return "Vault properties provider";
    }

    /**
     * Retrieve values from the environment when the pattern \$\[[^\]]*\] is used in a property value and replace the pattern
     * with the value. Example matches: $[ENV] or $[environment]
     *
     * @param value the text to search for the pattern and replace with values
     * @return the inserted text with environment variables looked up
     * @throws UnsetVariableException when the environment variable is not set
     */
    private String expandedValue(final String value) {
        String result = value;
        Matcher envMatcher = ENV_PATTERN.matcher(value);
        while (envMatcher.find()) {
            String envVariableName = envMatcher.group(1);
            String envValue = System.getenv(envVariableName);

            if (envValue == null) {
                envValue = System.getProperty(envVariableName);
                logger.debug("Retrieved environment value from property rather than environment");
            }

            if (envValue != null) {
                result = result.replaceAll("\\$\\[" + envVariableName + "\\]", envValue);
            } else {
                throw new UnsetVariableException(String.format("Environment variable [%s] is not set",envVariableName));
            }
        }

        return result;
    }

    // TODO: Add additional property to enable "Local Mode" that will then use a "Local Properties File" to load the properties
    private void evaluateLocalProperitesConfig(String localPropertiesFile){
        if(localPropertiesFile==null || localPropertiesFile.isEmpty())
            return;
        try {
            URL resourceUrl = this.getClass().getClassLoader().getResource(localPropertiesFile);
            if(resourceUrl==null) return;
            Properties appProps = new Properties();
            appProps.load(new FileInputStream(resourceUrl.getPath()));
            loadCacheFromProperties(appProps);
        } catch(Exception e) {
            logger.error(String.format("Error occurred while loading the local properties file from %s", localPropertiesFile), e);
        }
    }

    // TODO: Javadoc comments
    private void loadCacheFromProperties(Properties properties){
        if(properties == null || properties.isEmpty()) return;

        properties.entrySet().forEach(entry ->{
            String keyS = entry.getKey().toString();
            String[] pathElements = keyS.split("\\.");
            if(pathElements.length != 2){
                // TODO: Use String.format to format this string and also don't use all caps, also specify the format that should be used
                logger.warn("FAIL TO PARSE THE PROPERTY WITH THE KEY: "+ keyS);
            }else{
                if(cachedData.containsKey(pathElements[0]))
                    cachedData.get(pathElements[0]).put(pathElements[1], entry.getValue().toString());
                else
                    cachedData.put(pathElements[0], new HashMap<String,String>(){ { put(pathElements[1], entry.getValue().toString()); } });
            }
        });
    }
}
