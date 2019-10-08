package com.avioconsulting.mule.vault.provider.api.connection.provider;

import com.avioconsulting.mule.vault.provider.api.connection.VaultConnection;
import com.avioconsulting.mule.vault.provider.api.connection.impl.IamConnection;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.connection.ConnectionValidationResult;
import org.mule.runtime.config.api.dsl.model.ConfigurationParameters;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Summary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@DisplayName("IAM Connection")
@Alias("iam-connection")
public class IamConnectionProvider extends AbstractConnectionProvider {

    private static Logger LOGGER = LoggerFactory.getLogger(IamConnectionProvider.class);

    @DisplayName("Vault AWS Authentication Mount")
    @Summary("Mount point for AWS Authentication in Vault")
    @Parameter
    private String awsAuthMount;

    @DisplayName("Vault Role")
    @Summary("Name of the role against which the login is being attempted. If role is not specified, then the login " +
            "endpoint looks for a role bearing the name of the AMI ID of the EC2 instance that is trying to login if " +
            "using the ec2 auth method, or the \"friendly name\" (i.e., role name or username) of the IAM principal " +
            "authenticated. If a matching role is not found, login fails.")
    @Optional
    @Parameter
    private String vaultRole;

    @DisplayName("IAM Request URL")
    @Summary("Most likely https://sts.amazonaws.com/")
    @Parameter
    private String iamRequestUrl;

    @DisplayName("IAM Request Body")
    @Summary("Body of the signed request")
    @Parameter
    private String iamRequestBody;

    @DisplayName("IAM Request Headers")
    @Parameter
    private String iamRequestHeaders;

    public IamConnectionProvider() {
        super();
    }

    public IamConnectionProvider(ConfigurationParameters parameters) {
        super(parameters);

        try {
            awsAuthMount = parameters.getStringParameter("awsAuthMount");
            vaultRole = parameters.getStringParameter("vaultRole");
            iamRequestUrl = parameters.getStringParameter("iamRequestUrl");
            iamRequestBody = parameters.getStringParameter("iamRequestBody");
            iamRequestHeaders = parameters.getStringParameter("iamRequestHeaders");
        } catch (Exception e) {
            LOGGER.debug("All IAM properties must be present (iamAwsAuthMount, iamVaultRole, iamUrl, iamReqBody, iamReqHeaders)");
        }

    }

    @Override
    public VaultConnection connect() throws ConnectionException {
        return new IamConnection(vaultUrl, awsAuthMount, vaultRole, iamRequestUrl, iamRequestBody, iamRequestHeaders, sslProperties, engineVersion);
    }

    @Override
    public void disconnect(VaultConnection vaultConnection) {
        vaultConnection.invalidate();
    }

    @Override
    public ConnectionValidationResult validate(VaultConnection vaultConnection) {
        if (vaultConnection.isValid()) {
            return ConnectionValidationResult.success();
        } else {
            return ConnectionValidationResult.failure("Invalid Connection", null);
        }
    }
}
