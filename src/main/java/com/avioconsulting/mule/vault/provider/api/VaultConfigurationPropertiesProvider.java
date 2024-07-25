package com.avioconsulting.mule.vault.provider.api;

import com.avioconsulting.mule.vault.provider.internal.error.exception.SecretNotFoundException;
import com.avioconsulting.mule.vault.provider.internal.error.exception.UnsetVariableException;
import com.avioconsulting.mule.vault.provider.internal.error.exception.VaultAccessException;
import com.avioconsulting.mule.vault.provider.internal.extension.VaultPropertyPath;
import io.github.jopenlibs.vault.Vault;
import io.github.jopenlibs.vault.VaultException;
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
 * Provider to read Vault properties from the Vault server or fallback file.
 */
public class VaultConfigurationPropertiesProvider implements ConfigurationPropertiesProvider {

  private static final Logger logger = LoggerFactory.getLogger(VaultConfigurationPropertiesProvider.class);
  private static final String VAULT_PROPERTIES_PREFIX = "vault::";
  private static final Pattern VAULT_PATTERN = Pattern.compile(VAULT_PROPERTIES_PREFIX + "([^\\.]*)\\.([^}]*)");
  private static final Pattern ENV_PATTERN = Pattern.compile("\\$\\[([^\\]]*)\\]");

  private final Vault vault;

  private final boolean isLocalMode;

  Map<String, Map<String, String>> cachedData;

  /**
   * Constructs a VaultConfigurationPropertiesProvider. Vault must not be null.
   * If isLocalMode is true, it will use the fallback file which contain
   * secretPath and secrets.
   * All Vault connections will be disabled when isLocalMode be true.
   * 
   * @param vault
   *            vault object which contains secrets to pull from.
   * @param isLocalMode
   *            determines whether local fileback mode is enable or not.
   * @param localPropertiesFile
   *            local properties file name.
   */
  public VaultConfigurationPropertiesProvider(final Vault vault, final Boolean isLocalMode,
      final String localPropertiesFile) {
    this.vault = vault;
    cachedData = new HashMap<>();
    this.isLocalMode = isLocalMode;
    if (isLocalMode) {
      evaluateLocalProperitesConfig(localPropertiesFile);
    }
  }

  /**
   * Retrieves the property value from Vault. It stores the retrieved path in a
   * Map so a Dynamic Secrets can be used.
   *
   * @param path
   *            the path to the secret.
   * @param property
   *            the property to retrieve from the secret.
   * @return the value of the property or null if the property is not found.
   */
  private String getProperty(String path, String property)
      throws SecretNotFoundException, VaultAccessException, DefaultMuleException {
    try {
      Map<String, String> data = null;
      if (cachedData.containsKey(path)) {
        logger.trace("Getting data from cache");
        data = cachedData.get(path);
      } else if (!isLocalMode) {
        logger.trace("Getting data from Vault");
        // prefix= vault::namespace:engine_version:secret/path
        data = vault.logical().read(path).getData();
        // TODO: Does the driver ever return null? Or does it throw an exception? It
        // returns a null if there is no data stored in the secret
        cachedData.put(path, data);
      } else {
        // path is not in the cache and isLocalMode is true, so notify that the property
        // is not in the local properties file
        throw new SecretNotFoundException(
            String.format("No property found for %s.%s in the local properties file", path, property));
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
   * Get a configuration property value from Vault or a local properties file.
   *
   * @param configurationAttributeKey
   *            the key to lookup.
   * @return an {@link Optional} containing the {@link ConfigurationProperty}
   *         which holds
   *         the value for the given key at the given secret path.
   */
  @Override
  public Optional<ConfigurationProperty> getConfigurationProperty(String configurationAttributeKey) {
    Optional optionalValue = Optional.empty();
    if (configurationAttributeKey.startsWith(VAULT_PROPERTIES_PREFIX)) {
      VaultPropertyPath path = parsePropertyPath(configurationAttributeKey);
      if (path != null) {
        try {
          final String value = getProperty(path.getSecretPath(), path.getKey());
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
                return String.format("%s.%s", path.getSecretPath(), path.getKey());
              }
            });
          }
        } catch (Exception e) {
          logger.error("Property was not found", e);
        }
      }
    }
    return optionalValue;
  }

  @Override
  public String getDescription() {
    return "Vault properties provider";
  }

  /**
   * Parse the configurationAttributeKey to determine if the key provided should
   * be retrieved from Vault.
   * The configurationAttributeKey must match VAULT_PATTERN.
   * Not all configurationAttributeKeys are meant for the Vault Properties
   * Provider and they must be ignored.
   * 
   * @param configurationAttributeKey
   *            a String representing the secret path and key that should be
   *            parsed.
   * @return a {@link VaultPropertyPath} with the Vault Secret Path and Key to
   *         retrieve or null if it is not a Vault property.
   */
  private VaultPropertyPath parsePropertyPath(String configurationAttributeKey) {

    Matcher matcher = VAULT_PATTERN.matcher(configurationAttributeKey);
    if (matcher.find()) {
      // The Vault path is everything after the prefix and before the first period
      final String secretPath = matcher.group(1);

      // The key is everything after the first period
      final String key = matcher.group(2);

      return new VaultPropertyPath(expandedValue(secretPath), expandedValue(key));
    }

    return null;
  }

  /**
   * Retrieve values from the environment when the pattern \$\[[^\]]*\] is used in
   * a property value and replace the pattern
   * with the value. Example matches: $[ENV] or $[environment].
   *
   * @param value
   *            the text to search for the pattern and replace with values.
   * @return the inserted text with environment variables looked up.
   * @throws UnsetVariableException
   *             when the environment variable is not set.
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
        throw new UnsetVariableException(
            String.format("Environment variable [%s] is not set", envVariableName));
      }
    }

    return result;
  }

  /**
   * Read a properties file from localPropertiesFile and load the local cache with
   * its values.
   * 
   * @param localPropertiesFile
   *            path to a properties file located on the classpath.
   */
  private void evaluateLocalProperitesConfig(String localPropertiesFile) {
    if (localPropertiesFile == null || localPropertiesFile.isEmpty())
      return;
    try {
      URL resourceUrl = this.getClass().getClassLoader().getResource(localPropertiesFile);
      if (resourceUrl == null)
        return;
      Properties appProps = new Properties();
      appProps.load(new FileInputStream(resourceUrl.getPath()));
      loadCacheFromProperties(appProps);
    } catch (Exception e) {
      logger.error(String.format("Error occurred while loading the local properties file from %s",
          localPropertiesFile), e);
    }
  }

  /**
   * Populate the cache using the properties provided. Logs warnings if the
   * properties to not match the expected format.
   * Expected format: path/to/secret/engine.key.
   * 
   * Secret paths and the key cannot have periods in them.
   * 
   * @param properties
   *            A {@link Properties} object to load the cache with.
   */
  private void loadCacheFromProperties(Properties properties) {
    if (properties == null || properties.isEmpty())
      return;

    properties.entrySet().forEach(entry -> {
      String keyS = entry.getKey().toString();
      String[] pathElements = keyS.split("\\.");
      if (pathElements.length != 2) {
        logger.warn(String.format(
            "Invalid property secret path in local properties file (%s). Property must be in this format: path/to/secret/engine.key",
            keyS));
      } else {
        if (cachedData.containsKey(pathElements[0]))
          cachedData.get(pathElements[0]).put(pathElements[1], entry.getValue().toString());
        else
          cachedData.put(pathElements[0], new HashMap<String, String>() {
            {
              put(pathElements[1], entry.getValue().toString());
            }
          });
      }
    });
  }
}
