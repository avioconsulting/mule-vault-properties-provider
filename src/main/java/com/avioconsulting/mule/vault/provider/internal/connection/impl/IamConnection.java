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
import java.util.Base64;

public class IamConnection extends AbstractConnection {

    private static final String UTF_8 = "UTF-8";

    private static final Logger logger = LoggerFactory.getLogger(IamConnection.class);

    public IamConnection(String vaultUrl, String awsAuthMount, String role, String iamRequestUrl, String iamRequestBody,
                         String iamRequestHeaders, TlsContext tlsContext, EngineVersion engineVersion, int prefixPathDepth) throws ConnectionException {

        this.vaultConfig = new VaultConfig().address(vaultUrl).prefixPathDepth(prefixPathDepth);
        if (engineVersion != null) {
            this.vaultConfig = this.vaultConfig.engineVersion(engineVersion.getEngineVersionNumber());
        }

        try {
            SslConfig ssl = getVaultSSLConfig(tlsContext);
            this.vaultConfig = this.vaultConfig.sslConfig(ssl.build());
            logger.debug("TLS Setup Complete");

            Vault vaultDriver = new Vault(this.vaultConfig.build());

            String requestUrl_b64 = Base64.getEncoder().encodeToString(iamRequestUrl.getBytes(UTF_8));
            String requestBody_b64 = Base64.getEncoder().encodeToString(iamRequestBody.getBytes(UTF_8));

            AuthResponse response = vaultDriver.auth().loginByAwsIam(role, requestUrl_b64, requestBody_b64, iamRequestHeaders, awsAuthMount);
            this.vaultConfig = this.vaultConfig.token(response.getAuthClientToken());
            this.vault = new Vault(this.vaultConfig.build());
            this.valid = true;

        } catch (VaultException | CertificateException | NoSuchAlgorithmException | KeyStoreException | IOException e) {
            throw new ConnectionException(e);
        }
    }

}
