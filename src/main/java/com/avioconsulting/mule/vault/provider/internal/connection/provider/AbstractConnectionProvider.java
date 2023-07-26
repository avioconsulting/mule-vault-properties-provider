package com.avioconsulting.mule.vault.provider.internal.connection.provider;

import com.avioconsulting.mule.vault.provider.api.connection.parameters.EngineVersion;
import com.avioconsulting.mule.vault.provider.api.connection.parameters.TlsContext;
import com.avioconsulting.mule.vault.provider.internal.connection.VaultConnection;
import org.mule.runtime.api.connection.ConnectionProvider;
import org.mule.runtime.api.connection.ConnectionValidationResult;
import org.mule.runtime.api.meta.ExpressionSupport;
import org.mule.runtime.config.api.dsl.model.ConfigurationParameters;
import org.mule.runtime.extension.api.annotation.Expression;
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
    @Optional(defaultValue = "v2")
    protected EngineVersion engineVersion;

    @DisplayName("Local Mode Enabled")
    @Parameter
    @Optional(defaultValue = "false")
    @Expression(ExpressionSupport.SUPPORTED)
    protected boolean localMode;

    @DisplayName("Local Properties File")
    @Parameter
    @Optional
    protected String localPropertiesFile;

    @DisplayName("Prefix path depth")
    @Parameter
    @Optional(defaultValue = "1")
    protected int prefixPathDepth;

    protected abstract TlsContext getTlsContext();

    public AbstractConnectionProvider() {
        super();
    }

    public AbstractConnectionProvider(ConfigurationParameters parameters) {

        vaultUrl = parameters.getStringParameter("vaultUrl");

        // prefixPathDepth and localMode have default values, so the parameters will always be available
        prefixPathDepth = Integer.valueOf(parameters.getStringParameter("prefixPathDepth"));
        localMode = Boolean.parseBoolean(parameters.getStringParameter("localMode"));
        
        try {
            String ev = parameters.getStringParameter("engineVersion");
            engineVersion = EngineVersion.valueOf(ev);
        } catch (Exception e) {
            logger.debug("kvVersion parameter is not present, or is not a valid value (v1 or v2)", e);
        }

        // Only retrieve the local properties file path if local mode is enabled
        if (localMode) {
            try{
                localPropertiesFile = parameters.getStringParameter("localPropertiesFile");
            } catch (Exception e){
                logger.debug("localPropertiesFile parameter is not present", e);
            }
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

    public boolean isLocalMode() {
        return this.localMode;
    }

    public String getLocalPropertiesFile() {
        return this.localPropertiesFile;
    }
}
