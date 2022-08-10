package com.avioconsulting.mule.vault.provider.internal.connection.impl;

import com.avioconsulting.mule.vault.provider.api.connection.parameters.EngineVersion;
import com.avioconsulting.mule.vault.provider.api.connection.parameters.TlsContext;
import com.bettercloud.vault.SslConfig;
import com.bettercloud.vault.Vault;
import com.bettercloud.vault.VaultConfig;
import com.bettercloud.vault.VaultException;
import org.mule.runtime.api.connection.ConnectionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

public class TokenConnection extends AbstractConnection {
    private static final Logger logger = LoggerFactory.getLogger(TokenConnection.class);

    public TokenConnection(String vaultUrl, String vaultToken, TlsContext tlsContext, EngineVersion engineVersion) throws ConnectionException {

        try {
            this.vaultConfig = new VaultConfig().address(vaultUrl);
            if (engineVersion != null) {
                this.vaultConfig = this.vaultConfig.engineVersion(engineVersion.getEngineVersionNumber());
            }
            SslConfig ssl = getVaultSSLConfig(tlsContext);
            this.vault = new Vault(this.vaultConfig.token(vaultToken).sslConfig(ssl.build()).build());
            logger.debug("TLS Setup Complete");
            this.valid = true;
        } catch (VaultException | CertificateException | NoSuchAlgorithmException | KeyStoreException | IOException ve) {
            throw new ConnectionException(ve.getMessage(), ve.getCause());
        }
    }
}
