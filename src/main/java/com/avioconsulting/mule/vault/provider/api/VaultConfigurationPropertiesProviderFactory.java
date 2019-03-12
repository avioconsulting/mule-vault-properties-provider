/*
 * (c) 2003-2018 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.avioconsulting.mule.vault.provider.api;

import static org.mule.runtime.api.component.ComponentIdentifier.builder;

import com.bettercloud.vault.SslConfig;
import com.bettercloud.vault.VaultConfig;
import com.bettercloud.vault.VaultException;
import org.mule.runtime.api.component.ComponentIdentifier;
import org.mule.runtime.config.api.dsl.model.ConfigurationParameters;
import org.mule.runtime.config.api.dsl.model.ResourceProvider;
import org.mule.runtime.config.api.dsl.model.properties.ConfigurationPropertiesProvider;
import org.mule.runtime.config.api.dsl.model.properties.ConfigurationPropertiesProviderFactory;
import org.mule.runtime.config.api.dsl.model.properties.ConfigurationProperty;
import com.bettercloud.vault.Vault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Builds the provider for a vault-properties-provider:config element.
 *
 * @since 1.0
 */
public class VaultConfigurationPropertiesProviderFactory implements ConfigurationPropertiesProviderFactory {

  private final Logger LOGGER = LoggerFactory.getLogger(VaultConfigurationPropertiesProviderFactory.class);

  public static final String EXTENSION_NAMESPACE =
      VaultConfigurationPropertiesExtensionLoadingDelegate.EXTENSION_NAME.toLowerCase().replace(" ", "-");
  private static final ComponentIdentifier VAULT_PROPERTIES_PROVIDER =
      builder().namespace(EXTENSION_NAMESPACE).name(VaultConfigurationPropertiesExtensionLoadingDelegate.CONFIG_ELEMENT).build();
  // TODO change to meaningful prefix
  private final static String VAULT_PROPERTIES_PREFIX = "vault-properties-provider::";
  private final static Pattern VAULT_PATTERN = Pattern.compile(VAULT_PROPERTIES_PREFIX + "([^.}]*).(.*)");

  @Override
  public ComponentIdentifier getSupportedComponentIdentifier() {
    return VAULT_PROPERTIES_PROVIDER;
  }

  @Override
  public ConfigurationPropertiesProvider createProvider(final ConfigurationParameters parameters,
                                                        ResourceProvider externalResourceProvider) {

    return new ConfigurationPropertiesProvider() {

      @Override
      public Optional<ConfigurationProperty> getConfigurationProperty(String configurationAttributeKey) {

        if (configurationAttributeKey.startsWith(VAULT_PROPERTIES_PREFIX)) {
          Matcher matcher = VAULT_PATTERN.matcher(configurationAttributeKey);
          if (matcher.find()) {

            final String effectiveKey = configurationAttributeKey.substring(VAULT_PROPERTIES_PREFIX.length());

            // The Vault path is everything after the prefix and before the first period
            final String vaultPath = matcher.group(1);

            // The secret key is everything after the first period
            final String secretKey = matcher.group(2);


            try {
              Vault vault = getVault(parameters);
              final String value = vault.logical().read(vaultPath).getData().get(secretKey);

              return Optional.of(new ConfigurationProperty() {

                @Override
                public Object getSource() {
                  return "vault provider source";
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

            } catch (VaultException ve) {
              LOGGER.error("Error getting data from Vault", ve);
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
    };
  }

  /**
   * Get a vault connection based on the parameters provided by the user
   *
   * @param parameters The parameters read from the Mule config file
   * @return a fully configured {@link Vault} object
   */
  private Vault getVault(ConfigurationParameters parameters) throws VaultException {

    String vaultUrl = parameters.getStringParameter("vaultUrl");

    String vaultToken = null;
    String keyStorePath = null;
    String keyStorePassword = null;
    String trustStorePath = null;
    boolean verifySsl = false;

    // parameters.getStringParameter() stupidly throws a NullPointerException when the parameter is not present and
    // the getComplexConfigurationParameter() and getComplexConfigurationParameters() are for child elements, so
    // all we can really do is catch the exception when the optional parameters doesn't exist

    try {
      vaultToken = parameters.getStringParameter("vaultToken");
    } catch (Exception e) {
      LOGGER.debug("vaultToken parameter is not present");
    }

    try {
      String verifySslStr = parameters.getStringParameter("verifySSL");
      verifySsl = "true".equals(verifySslStr != null ? verifySslStr.toLowerCase() : "");
    } catch (Exception e) {
        LOGGER.debug("verifySSL parameter is not present");
    }

    try {
      keyStorePath = parameters.getStringParameter("keyStorePath");
      keyStorePassword = parameters.getStringParameter("keyStorePassword");
    } catch (Exception e) {
      LOGGER.debug("keyStorePath and/or keyStorePassword parameters are not present. Both are needed for TLS Authentication.");
    }

    try {
      trustStorePath = parameters.getStringParameter("trustStorePath");
    } catch (Exception e) {
      LOGGER.debug("trustStorePath parameter is not present");
    }

    VaultConfig vaultConfig = new VaultConfig().address(vaultUrl);

    if (vaultToken != null && !vaultToken.isEmpty()) {
      vaultConfig = vaultConfig.token(vaultToken);
    }

    SslConfig ssl = new SslConfig();
    if (keyStorePath != null && keyStorePassword != null && !keyStorePath.isEmpty() && !keyStorePassword.isEmpty()) {
      File keyStoreFile = new File(keyStorePath);
      if (keyStoreFile.exists() && keyStoreFile.isFile()) {
        ssl = ssl.keyStoreFile(keyStoreFile,keyStorePassword);
      } else {
        ssl = ssl.keyStoreResource(keyStorePath,keyStorePassword);
      }

    }
    if (trustStorePath != null && trustStorePath.isEmpty()) {
      File trustStoreFile = new File(trustStorePath);
      if (trustStoreFile.exists() && trustStoreFile.isFile()) {
        ssl = ssl.trustStoreFile(trustStoreFile);
      } else {
        ssl = ssl.trustStoreResource(trustStorePath);
      }
    }

    vaultConfig = vaultConfig.sslConfig(ssl.verify(verifySsl));

    final Vault vault = new Vault(vaultConfig.build());

    return vault;
  }

}
