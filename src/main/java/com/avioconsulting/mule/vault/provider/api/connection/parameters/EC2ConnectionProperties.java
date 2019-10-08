package com.avioconsulting.mule.vault.provider.api.connection.parameters;

import org.mule.runtime.config.api.dsl.model.ConfigurationParameters;
import org.mule.runtime.extension.api.annotation.param.ExclusiveOptionals;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Summary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Properties to be used for connecting via EC2 properties
 *
 * One, and only one, of the parameters must be set
 */
@ExclusiveOptionals(isOneRequired = true)
public class EC2ConnectionProperties {

    private static final Logger LOGGER = LoggerFactory.getLogger(EC2ConnectionProperties.class);

    @DisplayName("Use Instance Metadata")
    @Summary("Retrieve Instance metadata")
    @Parameter
    @Optional
    private boolean useInstanceMetadata = false;

    @DisplayName("PKCS7 Signature")
    @Summary("PKCS7 signature of the identity document with all \\n characters removed.")
    @Parameter
    @Optional
    private String pkcs7;

    @DisplayName("Identity Properties")
    @Summary("EC2 Identity Properties")
    @Parameter
    @Optional
    private AWSIdentityProperties identityProperties;

    public EC2ConnectionProperties() {
        super();
    }

    public EC2ConnectionProperties(ConfigurationParameters parameters) {
        super();

        try {
            pkcs7 = parameters.getStringParameter("pkcs7");
        } catch (Exception e) {
            LOGGER.debug("pkcs7 value is not set");
        }

        try {
            String useMetadataStr = parameters.getStringParameter("useInstanceMetadata");
            useInstanceMetadata = "true".equals(useMetadataStr);
        } catch (Exception e) {
            LOGGER.debug("useInstanceMetadata value is not set");
        }

        identityProperties = new AWSIdentityProperties(parameters);
    }

    public boolean isUseInstanceMetadata() {
        return useInstanceMetadata;
    }

    public void setUseInstanceMetadata(boolean useInstanceMetadata) {
        this.useInstanceMetadata = useInstanceMetadata;
    }

    public String getPkcs7() {
        return pkcs7;
    }

    public void setPkcs7(String pkcs7) {
        this.pkcs7 = pkcs7;
    }

    public AWSIdentityProperties getIdentityProperties() {
        return identityProperties;
    }

    public void setIdentityProperties(AWSIdentityProperties identityProperties) {
        this.identityProperties = identityProperties;
    }
}
