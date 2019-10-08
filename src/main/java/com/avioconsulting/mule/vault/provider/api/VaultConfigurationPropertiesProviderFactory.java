package com.avioconsulting.mule.vault.provider.api;

import com.avioconsulting.mule.vault.provider.api.connection.VaultConnection;
import com.avioconsulting.mule.vault.provider.api.connection.provider.Ec2ConnectionProvider;
import com.avioconsulting.mule.vault.provider.api.connection.provider.IamConnectionProvider;
import com.avioconsulting.mule.vault.provider.api.connection.provider.TlsConnectionProvider;
import com.avioconsulting.mule.vault.provider.api.connection.provider.TokenConnectionProvider;
import com.bettercloud.vault.Vault;
import org.mule.runtime.api.component.ComponentIdentifier;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.connection.ConnectionProvider;
import org.mule.runtime.config.api.dsl.model.ConfigurationParameters;
import org.mule.runtime.config.api.dsl.model.ResourceProvider;
import org.mule.runtime.config.api.dsl.model.properties.ConfigurationPropertiesProvider;
import org.mule.runtime.config.api.dsl.model.properties.ConfigurationPropertiesProviderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Builds the provider for a vault:config element.
 */
public class VaultConfigurationPropertiesProviderFactory implements ConfigurationPropertiesProviderFactory {

  private static final Logger LOGGER = LoggerFactory.getLogger(VaultConfigurationPropertiesProviderFactory.class);

  public static final String TOKEN_PARAMETER_GROUP = "token-connection";
  public static final String TLS_PARAMETER_GROUP = "tls-connection";
  public static final String IAM_PARAMETER_GROUP = "iam-connection";
  public static final String EC2_PARAMETER_GROUP = "ec2-connection";

  @Override
  public ComponentIdentifier getSupportedComponentIdentifier() {
    return VaultPropertiesProviderExtension.VAULT_PROPERTIES_PROVIDER;
  }

  @Override
  public ConfigurationPropertiesProvider createProvider(final ConfigurationParameters parameters,
                                                        ResourceProvider externalResourceProvider) {
    try {
      return new VaultConfigurationPropertiesProvider(getVault(parameters));
    } catch (ConnectionException ce) {
      LOGGER.error("Error connecting to Vault", ce);
      return null;
    }
  }

  /**
   * Get a vault connection based on the parameters provided by the user
   *
   * @param parameters The parameters read from the Mule config file
   * @return a fully configured {@link Vault} object
   */
  private Vault getVault(ConfigurationParameters parameters) throws ConnectionException {

    if (parameters.getComplexConfigurationParameters().size() > 1) {
      LOGGER.warn("Multiple Vault Properties Provider configurations have been found");
    }

    String firstConfiguation = parameters.getComplexConfigurationParameters().get(0).getFirst().getName();


    ConnectionProvider<VaultConnection> connectionProvider = null;

    ConfigurationParameters configurationParameters = parameters.getComplexConfigurationParameters().get(0).getSecond();
    if (TLS_PARAMETER_GROUP.equals(firstConfiguation)) {
      connectionProvider = new TlsConnectionProvider(configurationParameters);
    } else if (TOKEN_PARAMETER_GROUP.equals(firstConfiguation)) {
      connectionProvider = new TokenConnectionProvider(configurationParameters);
    } else if (IAM_PARAMETER_GROUP.equals(firstConfiguation)) {
      connectionProvider = new IamConnectionProvider(configurationParameters);
    } else if (EC2_PARAMETER_GROUP.equals(firstConfiguation)) {
      connectionProvider = new Ec2ConnectionProvider(configurationParameters);
    }

    if (connectionProvider != null) {
      return connectionProvider.connect().getVault();
    } else {
      LOGGER.warn("No Vault Properties Provider configurations found");
      return null;
    }

  }

}