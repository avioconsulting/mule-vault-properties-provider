package com.avioconsulting.mule.vault.provider.api.connection.impl;

import com.avioconsulting.mule.vault.provider.api.connection.parameters.EngineVersion;
import com.avioconsulting.mule.vault.provider.api.connection.parameters.SSLProperties;
import com.bettercloud.vault.SslConfig;
import com.bettercloud.vault.Vault;
import com.bettercloud.vault.VaultConfig;
import com.bettercloud.vault.VaultException;
import org.mule.runtime.api.connection.ConnectionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TokenConnection extends AbstractConnection {
    private final Logger logger = LoggerFactory.getLogger(TokenConnection.class);

    public TokenConnection(String vaultUrl, String vaultToken, SSLProperties sslProperties, EngineVersion engineVersion) throws ConnectionException {

        try {
            this.vaultConfig = new VaultConfig().address(vaultUrl);
            if (engineVersion != null) {
                this.vaultConfig = this.vaultConfig.engineVersion(engineVersion.getEngineVersionNumber());
            }
            SslConfig ssl = getVaultSSLConfig(sslProperties);
            this.vault = new Vault(this.vaultConfig.token(vaultToken).sslConfig(ssl.build()).build());
            this.valid = true;
        } catch (VaultException ve) {
            logger.error("Error establishing Vault connection", ve);
            throw new ConnectionException(ve.getMessage(), ve.getCause());
        }
    }
}
