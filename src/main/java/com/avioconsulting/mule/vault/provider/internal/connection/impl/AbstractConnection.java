package com.avioconsulting.mule.vault.provider.internal.connection.impl;

import com.avioconsulting.mule.vault.provider.internal.connection.VaultConnection;
import com.avioconsulting.mule.vault.provider.api.connection.parameters.SSLProperties;
import com.bettercloud.vault.SslConfig;
import com.bettercloud.vault.Vault;
import com.bettercloud.vault.VaultConfig;
import com.bettercloud.vault.VaultException;

import java.io.File;
import java.net.URL;

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
     * Construct {@link SslConfig} given the ssl-properties element for HTTPS connections to Vault
     *
     * @param sslProperties properties in the ssl-properties element
     * @return {@link SslConfig} constructed from the ssl-properties attributes
     * @throws VaultException if there is an error constructing the {@link SslConfig} object
     */
    public SslConfig getVaultSSLConfig(SSLProperties sslProperties) throws VaultException {
        SslConfig ssl = new SslConfig();
        if (sslProperties != null) {
            if (sslProperties.getPemFile() != null && !sslProperties.getPemFile().isEmpty()) {
                if (classpathResourceExists(sslProperties.getPemFile())) {
                    ssl = ssl.pemResource(sslProperties.getPemFile());
                } else {
                    ssl = ssl.pemFile(new File(sslProperties.getPemFile()));
                }
                ssl = ssl.verify(sslProperties.isVerifySSL());
            } else if (sslProperties.getTrustStoreFile() != null && !sslProperties.getTrustStoreFile().isEmpty()) {
                if (classpathResourceExists(sslProperties.getTrustStoreFile())) {
                    ssl = ssl.trustStoreResource(sslProperties.getTrustStoreFile());
                } else {
                    ssl = ssl.trustStoreFile(new File(sslProperties.getTrustStoreFile()));
                }
                ssl = ssl.verify(sslProperties.isVerifySSL());
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
    protected boolean classpathResourceExists(String path) {
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
