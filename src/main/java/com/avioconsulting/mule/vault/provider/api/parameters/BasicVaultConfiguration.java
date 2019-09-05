package com.avioconsulting.mule.vault.provider.api.parameters;

import com.avioconsulting.mule.vault.provider.api.parameters.group.EngineVersion;
import com.avioconsulting.mule.vault.provider.api.parameters.group.SSLProperties;
import com.bettercloud.vault.SslConfig;
import com.bettercloud.vault.Vault;
import com.bettercloud.vault.VaultConfig;
import com.bettercloud.vault.VaultException;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Placement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@DisplayName("Basic Connection")
@Alias("basic-connection")
public class BasicVaultConfiguration extends AbstractVaultConfiguration {

    private final Logger LOGGER = LoggerFactory.getLogger(BasicVaultConfiguration.class);

    @DisplayName("Vault URL")
    @Parameter
    private String vaultUrl;

    @DisplayName("Secrets Engine Version")
    @Parameter
    @Optional
    private EngineVersion engineVersion;

    @DisplayName("Vault Token")
    @Parameter
    private String vaultToken;

    @DisplayName("SSL Properties")
    @Parameter
    @Optional
    @Placement(tab = Placement.CONNECTION_TAB)
    private SSLProperties sslProperties;

    public Vault getVault() {
        try {
            this.vaultConfig = new VaultConfig().address(vaultUrl);
            if (engineVersion != null) {
                this.vaultConfig = this.vaultConfig.engineVersion(engineVersion.getEngineVersionNumber());
            }

            SslConfig ssl = getVaultSSLConfig(sslProperties);
            this.vault = new Vault(this.vaultConfig.token(vaultToken).sslConfig(ssl.build()).build());

        } catch (VaultException ve) {
            LOGGER.error("Error establishing Vault connection", ve);
        }
        return vault;
    }
}
