package com.avioconsulting.mule.vault.provider.api;

import com.avioconsulting.mule.vault.provider.internal.connection.VaultConnection;
import com.avioconsulting.mule.vault.provider.internal.connection.provider.*;
import com.avioconsulting.mule.vault.provider.internal.extension.VaultPropertiesProviderExtension;
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

import java.util.stream.Stream;

/**
 * Builds the provider for a vault:config element.
 */
public class VaultConfigurationPropertiesProviderFactory implements ConfigurationPropertiesProviderFactory {

  private static final Logger logger = LoggerFactory.getLogger(VaultConfigurationPropertiesProviderFactory.class);

  public static final String TOKEN_PARAMETER_GROUP = "token-connection";
  public static final String TLS_PARAMETER_GROUP = "tls-connection";
  public static final String IAM_PARAMETER_GROUP = "iam-connection";
  public static final String EC2_PARAMETER_GROUP = "ec2-connection";
  public static final String APPROLE_PARAMETER_GROUP= "approle-connection";

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
      logger.error("Error connecting to Vault", ce);
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
      logger.warn("Multiple Vault Properties Provider configurations have been found");
    }

    ConnectionProvider<VaultConnection> connectionProvider = null;
    
    /* 
      Issue #12: https://github.com/avioconsulting/mule-vault-properties-provider/issues/12
      Mule 4.4 added an expiration-policy element that is added automatically, so we need to 
      disregard it and find the correct configuration element. 
      See https://docs.mulesoft.com/mule-sdk/1.1/static-dynamic-configs
    */
    for (int i=0;i<parameters.getComplexConfigurationParameters().size();i++) {
	    String namespace = parameters.getComplexConfigurationParameters().get(i).getFirst().getNamespace();

	    if (namespace.equals(VaultPropertiesProviderExtension.VAULT_PROPERTIES_PROVIDER.getNamespace())) {
	    	String firstConfiguration = parameters.getComplexConfigurationParameters().get(i).getFirst().getName();
		    ConfigurationParameters configurationParameters = parameters.getComplexConfigurationParameters().get(i).getSecond();
		    if (TLS_PARAMETER_GROUP.equals(firstConfiguration)) {
		      connectionProvider = new TlsConnectionProvider(configurationParameters);
		    } else if (TOKEN_PARAMETER_GROUP.equals(firstConfiguration)) {
		      connectionProvider = new TokenConnectionProvider(configurationParameters);
		    } else if (IAM_PARAMETER_GROUP.equals(firstConfiguration)) {
		      connectionProvider = new IamConnectionProvider(configurationParameters);
		    } else if (EC2_PARAMETER_GROUP.equals(firstConfiguration)) {
		      connectionProvider = new Ec2ConnectionProvider(configurationParameters);
		    } else if(APPROLE_PARAMETER_GROUP.equals(firstConfiguration)){
              connectionProvider = new AppRoleConnectionProvider(configurationParameters);
            }
		    break;
	    }
    }

    if (connectionProvider != null) {
      return connectionProvider.connect().getVault();
    } else {
      logger.warn("No Vault Properties Provider configurations found");
      return null;
    }

  }

}