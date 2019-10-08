package com.avioconsulting.mule.vault.provider.api.connection.provider;

import com.avioconsulting.mule.vault.provider.api.connection.VaultConnection;
import com.avioconsulting.mule.vault.provider.api.connection.impl.Ec2Connection;
import com.bettercloud.vault.rest.Rest;
import com.bettercloud.vault.rest.RestException;
import com.bettercloud.vault.rest.RestResponse;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.connection.ConnectionValidationResult;
import org.mule.runtime.config.api.dsl.model.ConfigurationParameters;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Summary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

@DisplayName("EC2 Connection")
@Alias("ec2-connection")
public class Ec2ConnectionProvider extends AbstractConnectionProvider {

    // This is the URI to use to retrieve the PKCS7 Signature
    // See: https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/instance-identity-documents.html
    private final static String INSTANCE_PKCS7_URI = "http://169.254.169.254/latest/dynamic/instance-identity/pkcs7";
    private final Logger LOGGER = LoggerFactory.getLogger(Ec2ConnectionProvider.class);

    @DisplayName("Vault AWS Authentication Mount")
    @Summary("Mount point for AWS Authentication in Vault")
    @Parameter
    private String awsAuthMount;

    @DisplayName("Vault Role")
    @Parameter
    private String vaultRole;

    @DisplayName("PKCS7 Signature")
    @Summary("PKCS7 signature of the identity document with all \\n characters removed.")
    @Optional
    @Parameter
    private String pkcs7;

    @DisplayName("Identity Document")
    @Summary("Base64 encoded EC2 instance identity document.")
    @Optional
    @Parameter
    private String identity;

    @DisplayName("Identity Document Signature")
    @Summary("Base64 encoded SHA256 RSA signature of the instance identity document")
    @Optional
    @Parameter
    private String signature;

    @DisplayName("Use Instance Metadata")
    @Summary("Retrieve Instance metadata")
    @Parameter
    private boolean useInstanceMetadata = false;

    public Ec2ConnectionProvider() {
        super();
    }

    public Ec2ConnectionProvider(ConfigurationParameters parameters) {
        super(parameters);

        try {
            awsAuthMount = parameters.getStringParameter("awsAuthMount");
            vaultRole = parameters.getStringParameter("vaultRole");
        } catch (Exception e) {
            LOGGER.debug("ec2AwsAuthMount and ec2VaultRole are required for EC2 authentication");
        }

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

        try {
            identity = parameters.getStringParameter("identity");
            signature = parameters.getStringParameter("signature");
        } catch (Exception ide) {
            LOGGER.debug("identity and/or signature properties are not present. If one is set, both must be set");
        }
    }

    @Override
    public VaultConnection connect() throws ConnectionException {
        if (useInstanceMetadata) {
            pkcs7 = lookupPkcs7();
        }
        boolean pkcsUnavailable = pkcs7 == null || pkcs7.isEmpty();
        boolean identityUnavailable = identity == null || identity.isEmpty() || signature == null || signature.isEmpty();

        if (pkcsUnavailable && identityUnavailable) {
            LOGGER.error("PKCS7 Signature, Identity Document, and Identity Signature are all null or empty");
            throw new ConnectionException("PKCS7 Signature or the Identity Document and Signature are required");
        }

        return new Ec2Connection(vaultUrl, vaultRole, pkcs7, null, identity, signature, awsAuthMount, sslProperties, engineVersion);
    }

    @Override
    public void disconnect(VaultConnection vaultConnection) {
        vaultConnection.invalidate();
    }

    @Override
    public ConnectionValidationResult validate(VaultConnection vaultConnection) {
        if (vaultConnection.isValid()) {
            return ConnectionValidationResult.success();
        } else {
            return ConnectionValidationResult.failure("Invalid Connection", null);
        }
    }

    /**
     * EC2 Provides a service to retrieve the instance identity. This method uses that service to look up the PKCS7.
     *
     * @return the PKCS7 value with the '\n' characters removed
     */
    private String lookupPkcs7() {
        String pkcs7 = null;
        try {
            final RestResponse response = new Rest().url(INSTANCE_PKCS7_URI).get();
            String responseStr = new String(response.getBody(), StandardCharsets.UTF_8);
            // remove \n characters
            pkcs7 = responseStr.replaceAll("\n", "");
        } catch (RestException re) {
            LOGGER.error("Error looking up PKCS7 from Metadata Service",re);
        }
        return pkcs7;
    }
}
