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

import java.io.UnsupportedEncodingException;
import java.util.Base64;

public class IamConnection extends AbstractConnection {

    private final static String UTF_8 = "UTF-8";

    private final Logger LOGGER = LoggerFactory.getLogger(IamConnection.class);

    public IamConnection(String vaultUrl, String awsAuthMount, String role, String iamRequestUrl, String iamRequestBody,
                         String iamRequestHeaders, SSLProperties sslProperties, EngineVersion engineVersion) throws ConnectionException {

        this.vaultConfig = new VaultConfig().address(vaultUrl);
        if (engineVersion != null) {
            this.vaultConfig = this.vaultConfig.engineVersion(engineVersion.getEngineVersionNumber());
        }

        try {
            SslConfig ssl = getVaultSSLConfig(sslProperties);
            this.vaultConfig = this.vaultConfig.sslConfig(ssl.build());

            Vault vaultDriver = new Vault(this.vaultConfig.build());

            String requestUrl_b64 = Base64.getEncoder().encodeToString(iamRequestUrl.getBytes(UTF_8));
            String requestBody_b64 = Base64.getEncoder().encodeToString(iamRequestBody.getBytes(UTF_8));

            AuthResponse response = vaultDriver.auth().loginByAwsIam(role, requestUrl_b64, requestBody_b64, iamRequestHeaders, awsAuthMount);
            this.vaultConfig = this.vaultConfig.token(response.getAuthClientToken());
            this.vault = new Vault(this.vaultConfig.build());
            this.valid = true;

        } catch (VaultException | UnsupportedEncodingException e) {
            LOGGER.error("Error connecting to Vault", e);
            throw new ConnectionException(e);
        }
    }

}
