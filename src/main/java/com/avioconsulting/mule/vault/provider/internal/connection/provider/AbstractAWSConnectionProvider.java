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
    @Summary("Name of the role against which the login is being attempted. If role is not specified, then the login " +
            "endpoint looks for a role bearing the name of the AMI ID of the EC2 instance that is trying to login if " +
            "using the ec2 auth method, or the \"friendly name\" (i.e., role name or username) of the IAM principal " +
            "authenticated. If a matching role is not found, login fails.")
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
