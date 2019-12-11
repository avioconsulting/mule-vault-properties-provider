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
public class IamConnectionProvider extends AbstractAWSConnectionProvider {

    private static Logger logger = LoggerFactory.getLogger(IamConnectionProvider.class);

    @DisplayName("IAM Request URL")
    @Summary("Most likely https://sts.amazonaws.com/")
    @Parameter
    @Optional(defaultValue = "https://sts.amazonaws.com/")
    private String iamRequestUrl = "https://sts.amazonaws.com/";

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
            iamRequestUrl = parameters.getStringParameter("iamRequestUrl");
            iamRequestBody = parameters.getStringParameter("iamRequestBody");
            iamRequestHeaders = parameters.getStringParameter("iamRequestHeaders");
        } catch (Exception e) {
            logger.debug("All IAM properties must be present (iamAwsAuthMount, iamVaultRole, iamUrl, iamReqBody, iamReqHeaders)", e);
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
