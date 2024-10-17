package com.avioconsulting.mule.vault.provider.internal.connection.provider;

import org.mule.runtime.config.api.dsl.model.ConfigurationParameters;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Summary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract Class for common AWS Connection properties
 */
public abstract class AbstractAWSConnectionProvider extends AbstractConnectionProvider {

  private static final Logger logger = LoggerFactory.getLogger(AbstractAWSConnectionProvider.class);

  @DisplayName("Vault AWS Authentication Mount")
  @Summary("Mount point for AWS Authentication in Vault")
  @Parameter
  @Optional(defaultValue = "aws")
  protected String awsAuthMount;

  @DisplayName("Vault Role")
  @Summary("Name of the role against which the login is being attempted")
  @Optional
  @Parameter
  protected String vaultRole;

  public AbstractAWSConnectionProvider() {
    super();
  }

  public AbstractAWSConnectionProvider(ConfigurationParameters parameters) {
    super(parameters);

    try {
      this.awsAuthMount = parameters.getStringParameter("awsAuthMount");
    } catch (Exception e) {
      logger.debug("awsAuthMount is not specified, using default value", e);
    }

    try {
      this.vaultRole = parameters.getStringParameter("vaultRole");
    } catch (Exception e) {
      logger.debug("vaultRole is not specified, using the AMI ID of the EC2 instance", e);
    }
  }
}
