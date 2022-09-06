package com.avioconsulting.mule.vault.provider.internal.connection.impl;

import com.avioconsulting.mule.vault.provider.api.connection.parameters.EngineVersion;
import com.avioconsulting.mule.vault.provider.api.connection.parameters.TlsContext;
import com.bettercloud.vault.SslConfig;
import com.bettercloud.vault.Vault;
import com.bettercloud.vault.VaultConfig;
import com.bettercloud.vault.VaultException;
import com.bettercloud.vault.response.AuthResponse;
import org.mule.runtime.api.connection.ConnectionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

public class Ec2Connection extends AbstractConnection {

    private static final Logger logger = LoggerFactory.getLogger(Ec2Connection.class);

    public Ec2Connection(String vaultUrl, String role, String pkcs7, String nonce, String identity, String signature,
                         String awsAuthMount, TlsContext tlsContext, EngineVersion engineVersion, int prefixPathDepth) throws ConnectionException {

        this.vaultConfig = new VaultConfig().address(vaultUrl).prefixPathDepth(prefixPathDepth);
        if (engineVersion != null) {
            this.vaultConfig = this.vaultConfig.engineVersion(engineVersion.getEngineVersionNumber());
        }

        try {
            SslConfig ssl = getVaultSSLConfig(tlsContext);
            this.vaultConfig = this.vaultConfig.sslConfig(ssl.build());
            logger.debug("TLS Setup Complete");

            Vault vaultDriver = new Vault(this.vaultConfig.build());
            AuthResponse response = null;
            if (pkcs7 != null && !pkcs7.isEmpty()) {
                response = vaultDriver.auth().loginByAwsEc2(role, pkcs7, nonce, awsAuthMount);
            } else {
                response = vaultDriver.auth().loginByAwsEc2(role, identity, signature, nonce, awsAuthMount);
            }
            this.vaultConfig = this.vaultConfig.token(response.getAuthClientToken());
            this.vault = new Vault(this.vaultConfig.build());
            this.valid = true;
        } catch (VaultException | IOException | CertificateException | NoSuchAlgorithmException | KeyStoreException e) {
            throw new ConnectionException(e);
        }
    }
}
