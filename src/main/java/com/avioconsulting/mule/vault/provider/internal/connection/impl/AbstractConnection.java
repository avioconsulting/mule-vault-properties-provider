package com.avioconsulting.mule.vault.provider.internal.connection.impl;

import com.avioconsulting.mule.vault.provider.api.connection.parameters.KeyStoreConfig;
import com.avioconsulting.mule.vault.provider.api.connection.parameters.TlsContext;
import com.avioconsulting.mule.vault.provider.api.connection.parameters.TrustStoreConfig;
import com.avioconsulting.mule.vault.provider.internal.connection.VaultConnection;
import com.avioconsulting.vault.http.client.provider.VaultClient;
import org.glassfish.grizzly.ssl.SSLContextConfigurator;

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
     * Construct {@link SSLContextConfigurator} given the tls-context element for TLS connections to Vault
     *
     * @param tlsContext properties in the tls-context element
     * @return {@link SSLContextConfigurator} constructed from the tls-context attributes
     * @throws CertificateException
     * @throws NoSuchAlgorithmException
     * @throws KeyStoreException
     * @throws IOException
     */
    public SSLContextConfigurator getVaultSSLConfig(TlsContext tlsContext) throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException {
        SSLContextConfigurator ssl = new SSLContextConfigurator(false);
        if (tlsContext != null) {
            if (tlsContext.isTrustStoreConfigured()) {
                TrustStoreConfig trustStore = tlsContext.getTrustStoreConfig();
                ssl.setTrustStoreFile(trustStore.getPath());
                ssl.setTrustStorePass(trustStore.getPassword());
                ssl.setTrustStoreType(trustStore.getType());
            }
            if (tlsContext.isKeyStoreConfigured()) {
                KeyStoreConfig keyStore = tlsContext.getKeyStoreConfig();
                ssl.setKeyStoreFile(keyStore.getPath());
                ssl.setKeyStorePass(keyStore.getPassword());
                ssl.setKeyStoreType(keyStore.getType());
            }
        }
        return ssl;
    }

}
