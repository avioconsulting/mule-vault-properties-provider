package com.avioconsulting.mule.vault.provider.api.connection.impl;

import com.avioconsulting.mule.vault.provider.api.connection.parameters.EngineVersion;
import com.avioconsulting.mule.vault.provider.api.connection.parameters.SSLProperties;
import com.bettercloud.vault.SslConfig;
import com.bettercloud.vault.Vault;
import com.bettercloud.vault.VaultConfig;
import com.bettercloud.vault.VaultException;
import com.bettercloud.vault.response.AuthResponse;
import org.mule.runtime.api.connection.ConnectionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Ec2Connection extends AbstractConnection {

    private static final Logger logger = LoggerFactory.getLogger(Ec2Connection.class);

    public Ec2Connection(String vaultUrl, String role, String pkcs7, String nonce, String identity, String signature,
                         String awsAuthMount, SSLProperties sslProperties, EngineVersion engineVersion) throws ConnectionException {

        this.vaultConfig = new VaultConfig().address(vaultUrl);
        if (engineVersion != null) {
            this.vaultConfig = this.vaultConfig.engineVersion(engineVersion.getEngineVersionNumber());
        }

        try {
            SslConfig ssl = getVaultSSLConfig(sslProperties);
            this.vaultConfig = this.vaultConfig.sslConfig(ssl.build());
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
        } catch (VaultException ve) {
            logger.error("Error connecting to Vault", ve);
            throw new ConnectionException(ve);
        }
    }
}
