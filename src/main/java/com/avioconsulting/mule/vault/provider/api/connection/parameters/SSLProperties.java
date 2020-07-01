package com.avioconsulting.mule.vault.provider.api.connection.parameters;

import com.avioconsulting.mule.vault.provider.internal.extension.VaultPropertiesProviderExtension;
import org.mule.runtime.config.api.dsl.model.ConfigurationParameters;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Path;
import org.mule.runtime.extension.api.annotation.param.display.Summary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.mule.runtime.api.component.ComponentIdentifier.builder;

public class SSLProperties {

    private static final Logger logger = LoggerFactory.getLogger(SSLProperties.class);

    public static final String SSL_PARAMETER_GROUP = "ssl-properties";

    @DisplayName("Vault PEM File")
    @Summary("An X.509 certificate, to use when communicating with Vault over HTTPS")
    @Parameter
    @Optional
    @Path
    private String pemFile;

    @DisplayName("TrustStore File")
    @Parameter
    @Optional
    @Path
    private String trustStoreFile;

    @DisplayName("Verify SSL")
    @Optional(defaultValue = "true")
    @Parameter
    private boolean verifySSL = true;

    public SSLProperties() {
        super();
    }

    public SSLProperties(ConfigurationParameters parameters) {

        List<ConfigurationParameters> sslList = parameters
                .getComplexConfigurationParameter(builder()
                        .namespace(VaultPropertiesProviderExtension.EXTENSION_NAMESPACE)
                        .name(SSL_PARAMETER_GROUP).build());

        if (sslList.size() > 0) {
            ConfigurationParameters sslParameters = sslList.get(0);

            try {
                pemFile = sslParameters.getStringParameter("pemFile");
            } catch (Exception e) {
                logger.debug("pemFile parameter not present", e);
            }

            try {
                trustStoreFile = sslParameters.getStringParameter("trustStoreFile");
            } catch (Exception e) {
                logger.debug("trustStorePath parameter is not present", e);
            }

            try {
                verifySSL = "TRUE".equalsIgnoreCase(sslParameters.getStringParameter("verifySSL"));
            } catch (Exception e) {
                logger.debug("verifySSL parameter is not present", e);
            }
        }
    }

    public String getPemFile() {
        return pemFile;
    }

    public void setPemFile(String pemFile) {
        this.pemFile = pemFile;
    }

    public String getTrustStoreFile() {
        return trustStoreFile;
    }

    public void setTrustStoreFile(String trustStoreFile) {
        this.trustStoreFile = trustStoreFile;
    }

    public boolean isVerifySSL() {
        return verifySSL;
    }

    public void setVerifySSL(boolean verifySSL) {
        this.verifySSL = verifySSL;
    }
}