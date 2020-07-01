package com.avioconsulting.mule.vault.provider.internal.connection.provider;

import com.avioconsulting.mule.vault.provider.internal.connection.VaultConnection;
import com.avioconsulting.mule.vault.provider.internal.connection.impl.TokenConnection;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.connection.ConnectionValidationResult;
import org.mule.runtime.config.api.dsl.model.ConfigurationParameters;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@DisplayName("Token Connection")
@Alias("token-connection")
public class TokenConnectionProvider extends AbstractConnectionProvider {

    private static final Logger logger = LoggerFactory.getLogger(TokenConnectionProvider.class);

    @DisplayName("Vault Token")
    @Parameter
    private String vaultToken;

    public TokenConnectionProvider() {
        super();
    }

    public TokenConnectionProvider(ConfigurationParameters parameters) {
        super(parameters);

        try {
            vaultToken = parameters.getStringParameter("vaultToken");
        } catch (Exception e) {
            logger.debug("vaultToken parameter is not present", e);
        }
    }

    @Override
    public VaultConnection connect() throws ConnectionException {
        return new TokenConnection(vaultUrl, vaultToken, sslProperties, engineVersion);
    }

    @Override
    public void disconnect(VaultConnection connection) {
        connection.invalidate();
    }

    @Override
    public ConnectionValidationResult validate(VaultConnection connection) {
        if (connection.isValid()) {
            return ConnectionValidationResult.success();
        } else {
            return ConnectionValidationResult.failure("Connection Invalid", null);
        }
    }

}
