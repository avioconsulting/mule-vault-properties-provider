package com.avioconsulting.mule.vault.provider.api.parameters;

import com.avioconsulting.mule.vault.provider.api.parameters.group.*;
import com.bettercloud.vault.SslConfig;
import com.bettercloud.vault.Vault;
import com.bettercloud.vault.VaultConfig;
import com.bettercloud.vault.VaultException;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.ParameterGroup;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Placement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

@DisplayName("TLS Connection")
@Alias("tls-connection")
public class TlsVaultConfiguration extends AbstractVaultConfiguration {

    private final Logger LOGGER = LoggerFactory.getLogger(TlsVaultConfiguration.class);

    @DisplayName("Vault URL")
    @Parameter
    private String vaultUrl;

    @DisplayName("Secrets Engine Version")
    @Parameter
    @Optional
    private EngineVersion engineVersion;

    @ParameterGroup(name="TLS Authentication Parameters")
    private TLSAuthProperties tlsAuthProperties;

    @DisplayName("SSL Properties")
    @Parameter
    @Optional
    @Placement(tab = Placement.CONNECTION_TAB)
    private SSLProperties sslProperties;

    @Override
    public Vault getVault() {
        try {

            this.vaultConfig = new VaultConfig().address(vaultUrl);

            if (engineVersion != null) {
                this.vaultConfig = this.vaultConfig.engineVersion(engineVersion.getEngineVersionNumber());
            }

            SslConfig ssl = getVaultSSLConfig(sslProperties);

            JKSProperties jksProperties = tlsAuthProperties.getJksProperties();
            PEMProperties pemProperties = tlsAuthProperties.getPemProperties();

            if (jksProperties != null) {
                if (jksProperties.getKeyStoreFile() != null && jksProperties.getKeyStoreFile() != null
                        && !jksProperties.getKeyStorePassword().isEmpty()
                        && !jksProperties.getKeyStorePassword().isEmpty()) {
                    if (classpathResourceExists(jksProperties.getKeyStoreFile())) {
                        ssl = ssl.keyStoreResource(jksProperties.getKeyStoreFile(), jksProperties.getKeyStorePassword());
                    } else {
                        ssl = ssl.keyStoreFile(new File(jksProperties.getKeyStoreFile()), jksProperties.getKeyStorePassword());
                    }
                }
            } else if (pemProperties != null) {
                if (pemProperties.getClientPemFile() != null && !pemProperties.getClientPemFile().isEmpty()) {
                    if (classpathResourceExists(pemProperties.getClientPemFile())) {
                        ssl = ssl.clientPemResource(pemProperties.getClientPemFile());
                    } else {
                        ssl = ssl.clientPemFile(new File(pemProperties.getClientPemFile()));
                    }
                }
                if (pemProperties.getClientKeyPemFile() != null && !pemProperties.getClientKeyPemFile().isEmpty()) {
                    if (classpathResourceExists(pemProperties.getClientKeyPemFile())) {
                        ssl = ssl.clientKeyPemResource(pemProperties.getClientKeyPemFile());
                    } else {
                        ssl = ssl.clientKeyPemFile(new File(pemProperties.getClientKeyPemFile()));
                    }
                }
            }
            ssl = ssl.verify(true);
            this.vaultConfig = this.vaultConfig.sslConfig(ssl.build());
            Vault vaultDriver = new Vault(this.vaultConfig.build());
            String vaultToken = vaultDriver.auth().loginByCert().getAuthClientToken();
            this.vault = new Vault(this.vaultConfig.sslConfig(ssl.build()).token(vaultToken).build());
        } catch (VaultException ve) {
            LOGGER.error("Error creating Vault connection",ve);
        }
        return vault;

    }
}
