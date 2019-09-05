package com.avioconsulting.mule.vault.provider.api.parameters;

import com.avioconsulting.mule.vault.provider.api.parameters.group.EngineVersion;
import com.avioconsulting.mule.vault.provider.api.parameters.group.SSLProperties;
import com.bettercloud.vault.SslConfig;
import com.bettercloud.vault.Vault;
import com.bettercloud.vault.VaultConfig;
import com.bettercloud.vault.VaultException;
import com.bettercloud.vault.response.AuthResponse;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Placement;
import org.mule.runtime.extension.api.annotation.param.display.Summary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.util.Base64;

@DisplayName("IAM Connection")
@Alias("iam-connection")
public class IamVaultConfiguration extends AbstractVaultConfiguration {

    private final static String UTF_8 = "UTF-8";
    private final Logger LOGGER = LoggerFactory.getLogger(IamVaultConfiguration.class);

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
    @Summary("Name of the role against which the login is being attempted. If role is not specified, then the login " +
            "endpoint looks for a role bearing the name of the AMI ID of the EC2 instance that is trying to login if " +
            "using the ec2 auth method, or the \"friendly name\" (i.e., role name or username) of the IAM principal " +
            "authenticated. If a matching role is not found, login fails.")
    @Optional
    @Parameter
    private String vaultRole;

    @DisplayName("IAM Request URL")
    @Summary("Most likely https://sts.amazonaws.com/")
    @Parameter
    private String iamRequestUrl;

    @DisplayName("IAM Request Body")
    @Summary("Body of the signed request")
    @Parameter
    private String iamRequestBody;

    @DisplayName("IAM Request Headers")
    @Parameter
    private String iamRequestHeaders;

    @DisplayName("SSL Properties")
    @Parameter
    @Optional
    @Placement(tab = Placement.CONNECTION_TAB)
    private SSLProperties sslProperties;

    @Override
    public Vault getVault() {
        try {
            // iamRequestUrl and iamRequestBody need to be base64 encoded
            String requestUrl_b64 = Base64.getEncoder().encodeToString(iamRequestUrl.getBytes(UTF_8));
            String requestBody_b64 = Base64.getEncoder().encodeToString(iamRequestBody.getBytes(UTF_8));
            this.vaultConfig = new VaultConfig().address(vaultUrl);
            if (engineVersion != null) {
                this.vaultConfig = this.vaultConfig.engineVersion(engineVersion.getEngineVersionNumber());
            }
            SslConfig ssl = getVaultSSLConfig(sslProperties);
            this.vaultConfig = this.vaultConfig.sslConfig(ssl.build());
            Vault vaultDriver = new Vault(this.vaultConfig.build());
            AuthResponse response = vaultDriver.auth().loginByAwsIam(vaultRole, requestUrl_b64, requestBody_b64, iamRequestHeaders, awsAuthMount);
            this.vaultConfig = this.vaultConfig.token(response.getAuthClientToken());
            this.vault = new Vault(this.vaultConfig.build());

        } catch (VaultException ve) {
            LOGGER.error("Error connecting to Vault", ve);
        } catch (UnsupportedEncodingException e) {
            LOGGER.error("Error connecting to Vault", e);
        }
        return vault;
    }
}
