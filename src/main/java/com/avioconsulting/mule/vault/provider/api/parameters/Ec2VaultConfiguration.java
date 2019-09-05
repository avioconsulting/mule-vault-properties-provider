package com.avioconsulting.mule.vault.provider.api.parameters;

import com.avioconsulting.mule.vault.provider.api.parameters.group.EngineVersion;
import com.avioconsulting.mule.vault.provider.api.parameters.group.SSLProperties;
import com.bettercloud.vault.SslConfig;
import com.bettercloud.vault.Vault;
import com.bettercloud.vault.VaultConfig;
import com.bettercloud.vault.VaultException;
import com.bettercloud.vault.response.AuthResponse;
import com.bettercloud.vault.rest.Rest;
import com.bettercloud.vault.rest.RestException;
import com.bettercloud.vault.rest.RestResponse;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Placement;
import org.mule.runtime.extension.api.annotation.param.display.Summary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

@DisplayName("EC2 Connection")
@Alias("ec2-connection")
public class Ec2VaultConfiguration extends AbstractVaultConfiguration {
    // This is the URI to use to retrieve the PKCS7 Signature
    // See: https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/instance-identity-documents.html
    private final static String INSTANCE_PKCS7_URI = "http://169.254.169.254/latest/dynamic/instance-identity/pkcs7";
    private final Logger LOGGER = LoggerFactory.getLogger(Ec2VaultConfiguration.class);

    @DisplayName("Vault URL")
    @Parameter
    private String vaultUrl;

    @DisplayName("Secrets Engine Version")
    @Parameter
    @Optional
    private EngineVersion engineVersion;

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

    @DisplayName("SSL Properties")
    @Parameter
    @Optional
    @Placement(tab = Placement.CONNECTION_TAB)
    private SSLProperties sslProperties;

    /**
     * EC2 Provides a service to retrieve the instance identity. This method uses that service to look up the PKCS7.
     *
     * @return the PKCS7 value with the '\n' characters removed
     */
    private String lookupPKCS7() {
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

    @Override
    public Vault getVault() {
        this.vaultConfig = new VaultConfig().address(vaultUrl);
        if (engineVersion != null) {
            this.vaultConfig = this.vaultConfig.engineVersion(engineVersion.getEngineVersionNumber());
        }
        try {
            SslConfig ssl = getVaultSSLConfig(sslProperties);
            this.vaultConfig = this.vaultConfig.sslConfig(ssl.build());
            Vault vaultDriver = new Vault(this.vaultConfig.build());
            AuthResponse response = null;
            if (useInstanceMetadata) {
                pkcs7 = lookupPKCS7();
            }
            if (pkcs7 != null) {
                response = vaultDriver.auth().loginByAwsEc2(vaultRole, pkcs7, null, awsAuthMount);
            } else {
                response= vaultDriver.auth().loginByAwsEc2(vaultRole, identity, signature, null, awsAuthMount);
            }
            this.vaultConfig = this.vaultConfig.token(response.getAuthClientToken());
            this.vault = new Vault(this.vaultConfig.build());
        } catch (VaultException ve) {
            LOGGER.error("Error connecting to Vault", ve);
        }

        return vault;
    }


}
