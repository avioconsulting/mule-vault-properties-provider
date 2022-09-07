package com.avioconsulting.mule.vault.provider.internal.connection.impl;

import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import org.mule.runtime.api.connection.ConnectionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.avioconsulting.mule.vault.provider.api.connection.parameters.EngineVersion;
import com.avioconsulting.mule.vault.provider.api.connection.parameters.TlsContext;
import com.bettercloud.vault.SslConfig;
import com.bettercloud.vault.Vault;
import com.bettercloud.vault.VaultConfig;
import com.bettercloud.vault.VaultException;

public class TlsConnection extends AbstractConnection {

    private static final Logger logger = LoggerFactory.getLogger(TlsConnection.class);

    public TlsConnection(String vaultUrl, TlsContext tlsContext, EngineVersion engineVersion, int prefixPathDepth) throws ConnectionException {

        this.vaultConfig = new VaultConfig().address(vaultUrl).prefixPathDepth(prefixPathDepth);
        if (engineVersion != null) {
            this.vaultConfig = this.vaultConfig.engineVersion(engineVersion.getEngineVersionNumber());
        }

        try {
            SslConfig ssl = getVaultSSLConfig(tlsContext);
            this.vaultConfig = this.vaultConfig.sslConfig(ssl.build());
            logger.debug("TLS Setup Complete");
            Vault vaultDriver = new Vault(this.vaultConfig.build());
            String vaultToken = vaultDriver.auth().loginByCert().getAuthClientToken();
            this.vault = new Vault(this.vaultConfig.sslConfig(ssl.build()).token(vaultToken).build());
            this.valid = true;
        } catch (VaultException | CertificateException | NoSuchAlgorithmException | KeyStoreException | IOException ve) {
            throw new ConnectionException(ve);
        }
    }

}
