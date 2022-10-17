package com.avioconsulting.mule.vault.provider.internal.connection.impl;

import com.avioconsulting.mule.vault.provider.api.connection.parameters.EngineVersion;
import com.avioconsulting.mule.vault.provider.api.connection.parameters.TlsContext;
import com.avioconsulting.vault.http.client.exception.VaultException;
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

public class Ec2Connection extends AbstractConnection {

    private static final Logger logger = LoggerFactory.getLogger(Ec2Connection.class);

    public Ec2Connection(String vaultUrl, String role, String pkcs7, String nonce, String identity, String signature,
                         String awsAuthMount, TlsContext tlsContext, EngineVersion engineVersion, int prefixPathDepth) throws ConnectionException {

        try {
            SslConfig ssl = getVaultSSLConfig(tlsContext);
            logger.debug("TLS Setup Complete");
            this.vaultClient = new ClientProvider().getGrizzlyClient(vaultUrl, ssl.build(), 5000,
                    true, engineVersion.getEngineVersionNumber(), prefixPathDepth);

            AuthResponse response;

            if (pkcs7 != null && !pkcs7.isEmpty()) {
                response = vaultClient.authByAwsEc2(vaultUrl,role, nonce, pkcs7, awsAuthMount);
            } else {
                response = vaultClient.authByAwsEc2(vaultUrl,role, identity, signature,nonce, awsAuthMount);
            }
            this.vaultClient.setAuthToken(response.getClientToken());
            this.valid = true;
        } catch (CertificateException | NoSuchAlgorithmException | KeyStoreException | IOException ve) {
            throw new ConnectionException(ve.getMessage(), ve.getCause());
        } catch (VaultException e) {
            throw new RuntimeException(e);
        }

    }

    @Override public VaultClient getVaultClient() {
        return this.vaultClient;
    }
}
