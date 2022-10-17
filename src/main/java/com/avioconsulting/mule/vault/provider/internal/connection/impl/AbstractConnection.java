package com.avioconsulting.mule.vault.provider.internal.connection.impl;

import com.avioconsulting.mule.vault.provider.api.connection.parameters.TlsContext;
import com.avioconsulting.mule.vault.provider.internal.connection.VaultConnection;
import com.avioconsulting.vault.http.client.provider.VaultClient;
import com.avioconsulting.vault.http.client.ssl.SslConfig;

import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

public abstract class AbstractConnection implements VaultConnection {

    protected boolean valid = false;
    protected VaultClient vaultClient;

    public AbstractConnection() {
    }


    @Override
    public void invalidate() {
        this.valid = false;
        this.vaultClient = null;
    }

    @Override
    public boolean isValid() {
        return valid;
    }

    /**
     * Construct {@link SslConfig} given the tls-context element for TLS connections to Vault
     *
     * @param tlsContext properties in the tls-context element
     * @return {@link SslConfig} constructed from the tls-context attributes
     * @throws CertificateException
     * @throws NoSuchAlgorithmException
     * @throws KeyStoreException
     * @throws IOException
     */
    public SslConfig getVaultSSLConfig(TlsContext tlsContext) throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException {
        SslConfig ssl = new SslConfig();
        if (tlsContext != null) {
            if (tlsContext.isTrustStoreConfigured()) {
                ssl = ssl.trustStore(tlsContext.getTrustStoreConfig().getKeyStore()).
                        verify(!tlsContext.getTrustStoreConfig().isInsecure());
            }
            if (tlsContext.isKeyStoreConfigured()) {
                ssl = ssl.keyStore(tlsContext.getKeyStoreConfig().getKeyStore(), tlsContext.getKeyStoreConfig().getPassword());
            }
        }
        return ssl;
    }

}
