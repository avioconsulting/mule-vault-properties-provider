package com.avioconsulting.mule.vault.provider.internal.connection.impl;

import com.avioconsulting.mule.vault.provider.api.connection.parameters.EngineVersion;
import com.avioconsulting.mule.vault.provider.api.connection.parameters.TlsContext;
import com.avioconsulting.vault.http.client.output.AuthResponse;
import com.avioconsulting.vault.http.client.provider.ClientProvider;
import com.avioconsulting.vault.http.client.provider.VaultClient;
import com.avioconsulting.vault.http.client.ssl.SslConfig;
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

        try {
            SslConfig ssl = getVaultSSLConfig(tlsContext);
            logger.debug("TLS Setup Complete");
            this.vaultClient = new ClientProvider().getGrizzlyClient(vaultUrl, ssl.build(), 5000,
                    true, engineVersion.getEngineVersionNumber(), prefixPathDepth);

            String requestUrl_b64 = Base64.getEncoder().encodeToString(iamRequestUrl.getBytes(UTF_8));
            String requestBody_b64 = Base64.getEncoder().encodeToString(iamRequestBody.getBytes(UTF_8));

            AuthResponse authResponse = this.vaultClient.authByAwsIam(vaultUrl,role,requestUrl_b64,requestBody_b64
                    ,iamRequestHeaders,awsAuthMount);
            this.vaultClient.setAuthToken(authResponse.getClientToken());
            this.valid = true;
        } catch (CertificateException | NoSuchAlgorithmException | KeyStoreException | IOException ve) {
            throw new ConnectionException(ve.getMessage(), ve.getCause());
        } catch (com.avioconsulting.vault.http.client.exception.VaultException e) {
            throw new RuntimeException(e);
        }

    }

    @Override public VaultClient getVaultClient() {
        return this.vaultClient;
    }
}
