package com.avioconsulting.mule.vault.provider.api.connection.impl;

import com.avioconsulting.mule.vault.provider.api.connection.parameters.EngineVersion;
import com.avioconsulting.mule.vault.provider.api.connection.parameters.JKSProperties;
import com.avioconsulting.mule.vault.provider.api.connection.parameters.PEMProperties;
import com.avioconsulting.mule.vault.provider.api.connection.parameters.SSLProperties;
import com.bettercloud.vault.SslConfig;
import com.bettercloud.vault.Vault;
import com.bettercloud.vault.VaultConfig;
import com.bettercloud.vault.VaultException;
import org.mule.runtime.api.connection.ConnectionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class TlsConnection extends AbstractConnection {

    private Logger logger = LoggerFactory.getLogger(TlsConnection.class);

    public TlsConnection(String vaultUrl, JKSProperties jksProperties, PEMProperties pemProperties,
                         SSLProperties sslProperties, EngineVersion engineVersion) throws ConnectionException {

        this.vaultConfig = new VaultConfig().address(vaultUrl);

        if (engineVersion != null) {
            this.vaultConfig = this.vaultConfig.engineVersion(engineVersion.getEngineVersionNumber());
        }

        try {
            SslConfig ssl = getVaultSSLConfig(sslProperties);

            if (jksProperties != null && jksProperties.getKeyStoreFile() != null && jksProperties.getKeyStorePassword() != null) {
                if (!jksProperties.getKeyStoreFile().isEmpty() && !jksProperties.getKeyStorePassword().isEmpty()) {
                    if (classpathResourceExists(jksProperties.getKeyStoreFile())) {
                        ssl = ssl.keyStoreResource(jksProperties.getKeyStoreFile(), jksProperties.getKeyStorePassword());
                        logger.debug("Loading JKS key store from classpath");
                    } else {
                        ssl = ssl.keyStoreFile(new File(jksProperties.getKeyStoreFile()), jksProperties.getKeyStorePassword());
                        logger.debug("Loading JKS key store from file system");
                    }
                }
            } else if (pemProperties != null && pemProperties.getClientPemFile() != null && pemProperties.getClientKeyPemFile() != null) {
                if (!pemProperties.getClientPemFile().isEmpty()) {
                    if (classpathResourceExists(pemProperties.getClientPemFile())) {
                        ssl = ssl.clientPemResource(pemProperties.getClientPemFile());
                        logger.debug("Loading PEM file from classpath");
                    } else {
                        ssl = ssl.clientPemFile(new File(pemProperties.getClientPemFile()));
                        logger.debug("Loading PEM file from file system");
                    }
                }
                if (!pemProperties.getClientKeyPemFile().isEmpty()) {
                    if (classpathResourceExists(pemProperties.getClientKeyPemFile())) {
                        ssl = ssl.clientKeyPemResource(pemProperties.getClientKeyPemFile());
                        logger.debug("Loading key PEM file from classpath");
                    } else {
                        ssl = ssl.clientKeyPemFile(new File(pemProperties.getClientKeyPemFile()));
                        logger.debug("Loading key PEM file from file system");
                    }
                }
            }
            ssl = ssl.verify(true);
            this.vaultConfig = this.vaultConfig.sslConfig(ssl.build());
            Vault vaultDriver = new Vault(this.vaultConfig.build());
            String vaultToken = vaultDriver.auth().loginByCert().getAuthClientToken();
            this.vault = new Vault(this.vaultConfig.sslConfig(ssl.build()).token(vaultToken).build());
            this.valid = true;
        } catch (VaultException ve) {
            logger.error("Error creating Vault connection", ve);
            throw new ConnectionException(ve);
        }
    }

}
