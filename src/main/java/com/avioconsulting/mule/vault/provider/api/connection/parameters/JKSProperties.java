package com.avioconsulting.mule.vault.provider.api.connection.parameters;

import com.avioconsulting.mule.vault.provider.api.VaultPropertiesProviderExtension;
import org.mule.runtime.config.api.dsl.model.ConfigurationParameters;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Password;
import org.mule.runtime.extension.api.annotation.param.display.Path;
import org.mule.runtime.extension.api.annotation.param.display.Summary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.mule.runtime.api.component.ComponentIdentifier.builder;

public class JKSProperties {

    public static final Logger LOGGER = LoggerFactory.getLogger(JKSProperties.class);

    public static final String JKS_PARAMETER_GROUP = "jks-properties";

    @DisplayName("KeyStore File")
    @Summary("Path to the KeyStore if using Vault's TLS Certificate auth backend for client side authentication. The KeyStore password must also be provided.")
    @Path
    @Parameter
    private String keyStoreFile;

    @DisplayName("KeyStore Password")
    @Password
    @Parameter
    private String keyStorePassword;

    public JKSProperties() {
        super();
    }

    public JKSProperties(ConfigurationParameters parameters) {
        super();

        List<ConfigurationParameters> jksList = parameters
                .getComplexConfigurationParameter(builder()
                        .namespace(VaultPropertiesProviderExtension.EXTENSION_NAMESPACE)
                        .name(JKS_PARAMETER_GROUP).build());

        if (jksList.size() > 0) {
            ConfigurationParameters jksParameters = jksList.get(0);

            try {
                keyStoreFile = jksParameters.getStringParameter("keyStoreFile");
            } catch (Exception e) {
                LOGGER.error("keyStoreFile property is not set");
            }

            try {
                keyStorePassword = jksParameters.getStringParameter("keyStorePassword");
            } catch (Exception e) {
                LOGGER.error("keyStorePassword property is not set");
            }
        }
    }

    public String getKeyStoreFile() {
        return keyStoreFile;
    }

    public void setKeyStoreFile(String keyStoreFile) {
        this.keyStoreFile = keyStoreFile;
    }

    public String getKeyStorePassword() {
        return keyStorePassword;
    }

    public void setKeyStorePassword(String keyStorePassword) {
        this.keyStorePassword = keyStorePassword;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("keyStoreFile: ");
        sb.append(keyStoreFile);
        sb.append(", keyStorePassword: ");
        sb.append(keyStorePassword);
        return  sb.toString();
    }
}
