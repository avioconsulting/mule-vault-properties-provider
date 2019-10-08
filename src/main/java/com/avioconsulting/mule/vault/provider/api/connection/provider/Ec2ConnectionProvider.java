package com.avioconsulting.mule.vault.provider.api.connection.provider;

import com.avioconsulting.mule.vault.provider.api.connection.VaultConnection;
import com.avioconsulting.mule.vault.provider.api.connection.impl.Ec2Connection;
import com.avioconsulting.mule.vault.provider.api.connection.parameters.EC2ConnectionProperties;
import com.bettercloud.vault.rest.Rest;
import com.bettercloud.vault.rest.RestException;
import com.bettercloud.vault.rest.RestResponse;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.connection.ConnectionValidationResult;
import org.mule.runtime.config.api.dsl.model.ConfigurationParameters;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.param.ExclusiveOptionals;
import org.mule.runtime.extension.api.annotation.param.ParameterGroup;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

@DisplayName("EC2 Connection")
@Alias("ec2-connection")
@ExclusiveOptionals(isOneRequired = true)
public class Ec2ConnectionProvider extends AbstractAWSConnectionProvider {

    // This is the URI to use to retrieve the PKCS7 Signature
    // See: https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/instance-identity-documents.html
    private final static String INSTANCE_PKCS7_URI = "http://169.254.169.254/latest/dynamic/instance-identity/pkcs7";
    private final Logger LOGGER = LoggerFactory.getLogger(Ec2ConnectionProvider.class);

    @ParameterGroup(name = "EC2 Properties")
    EC2ConnectionProperties connectionProperties;

    public Ec2ConnectionProvider() {
        super();
    }

    public Ec2ConnectionProvider(ConfigurationParameters parameters) {
        super(parameters);

        connectionProperties = new EC2ConnectionProperties(parameters);
    }

    @Override
    public VaultConnection connect() throws ConnectionException {
        if (connectionProperties != null) {
            if (connectionProperties.isUseInstanceMetadata()) {
                connectionProperties.setPkcs7(lookupPkcs7());
            }
            boolean pkcsUnavailable = connectionProperties.getPkcs7() == null || connectionProperties.getPkcs7().isEmpty();
            boolean identityUnavailable = connectionProperties.getIdentityProperties().getIdentity() == null
                    || connectionProperties.getIdentityProperties().getIdentity().isEmpty()
                    || connectionProperties.getIdentityProperties().getSignature() == null
                    || connectionProperties.getIdentityProperties().getSignature().isEmpty();

            if (pkcsUnavailable && identityUnavailable) {
                LOGGER.error("PKCS7 Signature, Identity Document, and Identity Signature are all null or empty");
                throw new ConnectionException("PKCS7 Signature or the Identity Document and Signature are required");
            }

            return new Ec2Connection(vaultUrl,
                    vaultRole,
                    connectionProperties.getPkcs7(),
                    null,
                    connectionProperties.getIdentityProperties().getIdentity(),
                    connectionProperties.getIdentityProperties().getSignature(),
                    awsAuthMount,
                    sslProperties,
                    engineVersion);
        } else {
            return null;
        }
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
