package com.avioconsulting.mule.vault.provider.internal.connection.impl;

import com.avioconsulting.mule.vault.provider.api.connection.parameters.TlsContext;
import com.avioconsulting.mule.vault.provider.internal.connection.VaultConnection;
import com.bettercloud.vault.SslConfig;
import com.bettercloud.vault.Vault;
import com.bettercloud.vault.VaultConfig;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

public abstract class AbstractConnection implements VaultConnection {

    protected boolean valid = false;
    protected Vault vault;
    protected VaultConfig vaultConfig;

    public AbstractConnection() {
        vault = null;
        vaultConfig = new VaultConfig();
    }

    @Override
    public Vault getVault() {
        return vault;
    }

    @Override
    public void invalidate() {
        this.valid = false;
        this.vault = null;
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

    /**
     * Determine if the path resides on the classpath
     *
     * @param path the path to the file
     * @return true if the file is on the classpath
     */
    boolean classpathResourceExists(String path) {
        boolean fileExists = false;
        URL fileUrl = getClass().getResource(path);
        if (fileUrl != null) {
            File file = new File(fileUrl.getFile());
            if (file != null) {
                fileExists = file.exists();
            }
        }
        return fileExists;
    }
}
