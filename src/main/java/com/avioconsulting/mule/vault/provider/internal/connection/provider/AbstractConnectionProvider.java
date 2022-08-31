package com.avioconsulting.mule.vault.provider.internal.connection.provider;

import com.avioconsulting.mule.vault.provider.api.connection.parameters.EngineVersion;
import com.avioconsulting.mule.vault.provider.api.connection.parameters.TlsContext;
import com.avioconsulting.mule.vault.provider.internal.connection.VaultConnection;
import org.mule.runtime.api.connection.ConnectionProvider;
import org.mule.runtime.api.connection.ConnectionValidationResult;
import org.mule.runtime.config.api.dsl.model.ConfigurationParameters;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractConnectionProvider implements ConnectionProvider<VaultConnection> {

    private static final Logger logger = LoggerFactory.getLogger(AbstractConnectionProvider.class);

    @DisplayName("Vault URL")
    @Parameter
    protected String vaultUrl;

    @DisplayName("Secrets Engine Version")
    @Parameter
    @Optional
    protected EngineVersion engineVersion;

    @DisplayName("Fallback File Path")
    @Parameter
    @Optional
    protected String fallBackFile;

    protected abstract TlsContext getTlsContext();

    public AbstractConnectionProvider() {
        super();
    }

    public AbstractConnectionProvider(ConfigurationParameters parameters) {

        vaultUrl = parameters.getStringParameter("vaultUrl");

        try {
            String ev = parameters.getStringParameter("engineVersion");
            engineVersion = EngineVersion.valueOf(ev);
        } catch (Exception e) {
            logger.debug("kvVersion parameter is not present, or is not a valid value (v1 or v2)", e);
        }
        try{
            fallBackFile = parameters.getStringParameter("fallBackFile");
        } catch (Exception e){
            logger.debug("fileFallback parameter is not present", e);
        }
    }

    @Override
    public void disconnect(VaultConnection connection) {
        connection.invalidate();
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
