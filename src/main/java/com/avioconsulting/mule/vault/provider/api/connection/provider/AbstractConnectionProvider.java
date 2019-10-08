package com.avioconsulting.mule.vault.provider.api.connection.provider;

import com.avioconsulting.mule.vault.provider.api.connection.VaultConnection;
import com.avioconsulting.mule.vault.provider.api.connection.parameters.EngineVersion;
import com.avioconsulting.mule.vault.provider.api.connection.parameters.SSLProperties;
import org.mule.runtime.api.connection.ConnectionProvider;
import org.mule.runtime.config.api.dsl.model.ConfigurationParameters;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Placement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractConnectionProvider implements ConnectionProvider<VaultConnection> {

    private Logger LOGGER = LoggerFactory.getLogger(AbstractConnectionProvider.class);

    @DisplayName("Vault URL")
    @Parameter
    protected String vaultUrl;

    @DisplayName("Secrets Engine Version")
    @Parameter
    @Optional
    protected EngineVersion engineVersion;

    @DisplayName("SSL Properties")
    @Parameter
    @Optional
    @Placement(tab = Placement.CONNECTION_TAB)
    protected SSLProperties sslProperties;

    public AbstractConnectionProvider() {
        super();
    }

    public AbstractConnectionProvider(ConfigurationParameters parameters) {

        sslProperties = new SSLProperties(parameters);

        vaultUrl = parameters.getStringParameter("vaultUrl");

        try {
            String ev = parameters.getStringParameter("engineVersion");
            engineVersion = EngineVersion.valueOf(ev);
        } catch (Exception e) {
            LOGGER.debug("kvVersion parameter is not present, or is not a valid value (v1 or v2)");
        }

    }



}
