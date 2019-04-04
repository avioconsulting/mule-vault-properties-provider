/*
 * (c) 2003-2018 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.avioconsulting.mule.vault.provider.api;

import static org.mule.runtime.api.component.ComponentIdentifier.builder;

import com.bettercloud.vault.SslConfig;
import com.bettercloud.vault.VaultConfig;
import com.bettercloud.vault.VaultException;
import com.bettercloud.vault.rest.Rest;
import com.bettercloud.vault.rest.RestException;
import com.bettercloud.vault.rest.RestResponse;
import org.mule.runtime.api.component.ComponentIdentifier;
import org.mule.runtime.config.api.dsl.model.ConfigurationParameters;
import org.mule.runtime.config.api.dsl.model.ResourceProvider;
import org.mule.runtime.config.api.dsl.model.properties.ConfigurationPropertiesProvider;
import org.mule.runtime.config.api.dsl.model.properties.ConfigurationPropertiesProviderFactory;
import org.mule.runtime.config.api.dsl.model.properties.ConfigurationProperty;
import com.bettercloud.vault.Vault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Builds the provider for a vault-properties-provider:config element.
 *
 * @since 1.0
 */
public class VaultConfigurationPropertiesProviderFactory implements ConfigurationPropertiesProviderFactory {

  // This is the URI to use to retrieve the PKCS7 Signature
  // See: https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/instance-identity-documents.html
  private final static String INSTANCE_PKCS7_URI = "http://169.254.169.254/latest/dynamic/instance-identity/pkcs7";

  private final Logger LOGGER = LoggerFactory.getLogger(VaultConfigurationPropertiesProviderFactory.class);

  public static final String EXTENSION_NAMESPACE =
      VaultConfigurationPropertiesExtensionLoadingDelegate.EXTENSION_NAME.toLowerCase().replace(" ", "-");
  private static final ComponentIdentifier VAULT_PROPERTIES_PROVIDER =
      builder().namespace(EXTENSION_NAMESPACE).name(VaultConfigurationPropertiesExtensionLoadingDelegate.CONFIG_ELEMENT).build();
  // TODO change to meaningful prefix
  private final static String VAULT_PROPERTIES_PREFIX = "vault::";
  private final static Pattern VAULT_PATTERN = Pattern.compile(VAULT_PROPERTIES_PREFIX + "([^.}]*).(.*)");

  @Override
  public ComponentIdentifier getSupportedComponentIdentifier() {
    return VAULT_PROPERTIES_PROVIDER;
  }

  @Override
  public ConfigurationPropertiesProvider createProvider(final ConfigurationParameters parameters,
                                                        ResourceProvider externalResourceProvider) {

    return new ConfigurationPropertiesProvider() {

      @Override
      public Optional<ConfigurationProperty> getConfigurationProperty(String configurationAttributeKey) {

        if (configurationAttributeKey.startsWith(VAULT_PROPERTIES_PREFIX)) {
          Matcher matcher = VAULT_PATTERN.matcher(configurationAttributeKey);
          if (matcher.find()) {

            final String effectiveKey = configurationAttributeKey.substring(VAULT_PROPERTIES_PREFIX.length());

            // The Vault path is everything after the prefix and before the first period
            final String vaultPath = matcher.group(1);

            // The secret key is everything after the first period
            final String secretKey = matcher.group(2);


            try {
              final Vault vault = getVault(parameters);
              final String value = vault.logical().read(vaultPath).getData().get(secretKey);

              return Optional.of(new ConfigurationProperty() {

                @Override
                public Object getSource() {
                  return "vault provider source";
                }

                @Override
                public Object getRawValue() {
                  return value;
                }

                @Override
                public String getKey() {
                  return effectiveKey;
                }
              });

            } catch (VaultException ve) {
              LOGGER.error("Error getting data from Vault", ve);
            }

            return Optional.empty();

          }
        }
        return Optional.empty();
      }

      @Override
      public String getDescription() {
        return "Vault properties provider";
      }
    };
  }

  /**
   * Get a vault connection based on the parameters provided by the user
   *
   * @param parameters The parameters read from the Mule config file
   * @return a fully configured {@link Vault} object
   */
  private Vault getVault(ConfigurationParameters parameters) throws VaultException {

    String vaultUrl = parameters.getStringParameter("vaultUrl");

    VaultConfig vaultConfig = new VaultConfig().address(vaultUrl);

    List<ConfigurationParameters> sslList = parameters
            .getComplexConfigurationParameter(ComponentIdentifier.builder()
                    .namespace(EXTENSION_NAMESPACE)
                    .name(VaultConfigurationPropertiesExtensionLoadingDelegate.SSL_PARAMETER_GROUP).build());

    List<ConfigurationParameters> basicList = parameters
            .getComplexConfigurationParameter(ComponentIdentifier.builder()
                    .namespace(EXTENSION_NAMESPACE)
                    .name(VaultConfigurationPropertiesExtensionLoadingDelegate.BASIC_PARAMETER_GROUP).build());

    List<ConfigurationParameters> iamList = parameters
            .getComplexConfigurationParameter(ComponentIdentifier.builder()
                    .namespace(EXTENSION_NAMESPACE)
                    .name(VaultConfigurationPropertiesExtensionLoadingDelegate.IAM_PARAMETER_GROUP).build());

    List<ConfigurationParameters> ec2List = parameters
            .getComplexConfigurationParameter(ComponentIdentifier.builder()
                    .namespace(EXTENSION_NAMESPACE)
                    .name(VaultConfigurationPropertiesExtensionLoadingDelegate.EC2_PARAMETER_GROUP).build());

    if(sslList.size() > 0) {
      vaultConfig = getSSLVaultConfig(vaultConfig, sslList.get(0));
    }

    if (vaultConfig.getToken() == null || vaultConfig.getToken().isEmpty()) {
      if (basicList.size() > 0) {
        vaultConfig = getBasicVaultConfig(vaultConfig, basicList.get(0));
      } else if (iamList.size() > 0) {
        vaultConfig = getIamVaultConfig(vaultConfig, iamList.get(0));
      } else if (ec2List.size() > 0) {
        vaultConfig = getEc2VaultConfig(vaultConfig, ec2List.get(0));
      }
    }

    try {
      final Vault vault = new Vault(vaultConfig.build());
      return vault;
    } catch (VaultException ve){
      System.out.println("Error connecting to Vault at " + vaultConfig.getAddress() + " with token (" + vaultConfig.getToken() + ")");
      throw ve;
    }
  }

  /**
   * Get VaultConfig from the basic parameters
   * @param vaultConfig current state of the VaultConfig
   * @param basicParameters parameters from the basic element
   * @return VaultConfig with additional parameters added to it
   */
  private VaultConfig getBasicVaultConfig(VaultConfig vaultConfig, ConfigurationParameters basicParameters) {

    // parameters.getStringParameter() stupidly throws a NullPointerException when the parameter is not present and
    // the getComplexConfigurationParameter() and getComplexConfigurationParameters() are for child elements, so
    // all we can really do is catch the exception when the optional parameters doesn't exist

    try {
      vaultConfig = vaultConfig.engineVersion(new Integer(basicParameters.getStringParameter("kvVersion")));
    } catch (Exception e) {
      LOGGER.debug("kvVersion parameter is not present, or is not a valid value (1 or 2)");
    }
    try {
      vaultConfig = vaultConfig.token(basicParameters.getStringParameter("vaultToken"));
    } catch (Exception e) {
      LOGGER.debug("vaultToken parameter is not present");
    }
    return vaultConfig;
  }

  /**
   * Get VaultConfig from ssl parameters
   * @param vaultConfig current state of the VaultConfig
   * @param sslParameters parameters from the ssl element
   * @return VaultConfig with additional parameters added to it
   * @throws VaultException if there is an issue authenticating with a certificate
   */
  private VaultConfig getSSLVaultConfig(VaultConfig vaultConfig, ConfigurationParameters sslParameters) throws VaultException {

    String keyStorePath = null;
    String keyStorePassword = null;
    String trustStorePath = null;
    String pemFilePath = null;
    String clientPemFile = null;
    String clientKeyPemFile = null;
    boolean verifySsl = false;
    boolean useTlsAuthentication = false;

    try {
      pemFilePath = sslParameters.getStringParameter("pemFile");
    } catch (Exception e) {
      LOGGER.debug("pemFile parameter not present");
    }

    try {
      clientPemFile = sslParameters.getStringParameter("clientPemFile");
      clientKeyPemFile = sslParameters.getStringParameter("clientKeyPemFile");
    } catch (Exception e) {
      LOGGER.debug("clientPemFile and/or clientKeyPemFile parameters not present");
    }

    try {
      keyStorePath = sslParameters.getStringParameter("keyStorePath");
      keyStorePassword = sslParameters.getStringParameter("keyStorePassword");
      trustStorePath = sslParameters.getStringParameter("trustStorePath");
    } catch (Exception e) {
      LOGGER.debug("keyStorePath, keyStorePassword, and/or trustStorePath parameters are not present. All are needed for TLS Authentication.");
    }

    try {
      String tlsAuthStr = sslParameters.getStringParameter("useTlsAuth");
      useTlsAuthentication = "true".equals(tlsAuthStr != null ? tlsAuthStr.toLowerCase() : "");
    } catch (Exception e) {
      LOGGER.debug("useTlsAuth parameter is not present");
    }

    try {
      String verifySslStr = sslParameters.getStringParameter("verifySSL");
      verifySsl = "true".equals(verifySslStr != null ? verifySslStr.toLowerCase() : "");
    } catch (Exception e) {
      LOGGER.debug("verifySSL parameter is not present");
    }

    SslConfig ssl = new SslConfig();

    // If useTlsAuth is true, verifySsl must also be true, or it will fail to authenticate
    ssl = ssl.verify(verifySsl || useTlsAuthentication);

    if (pemFilePath != null && !pemFilePath.isEmpty()) {
      ssl = classpathResourceExists(pemFilePath) ?
              ssl.pemResource(pemFilePath) :
              ssl.pemFile(new File(pemFilePath));
    }

    if (clientPemFile != null && !clientPemFile.isEmpty()
          && clientKeyPemFile != null && !clientKeyPemFile.isEmpty()) {
      ssl = classpathResourceExists(clientPemFile) ?
              ssl.clientPemResource(clientPemFile) :
              ssl.clientPemFile(new File(clientPemFile));
      ssl = classpathResourceExists(clientKeyPemFile) ?
              ssl.clientKeyPemResource(clientKeyPemFile) :
              ssl.clientKeyPemFile(new File(clientKeyPemFile));
    }

    if (keyStorePath != null && keyStorePassword != null && trustStorePath != null
            && !keyStorePath.isEmpty() && !keyStorePassword.isEmpty() && !trustStorePath.isEmpty()) {
      ssl = classpathResourceExists(keyStorePath) ?
              ssl.keyStoreResource(keyStorePath, keyStorePassword) :
              ssl.keyStoreFile(new File(keyStorePath), keyStorePassword);

      ssl = classpathResourceExists(trustStorePath) ?
              ssl.trustStoreResource(trustStorePath) :
              ssl.trustStoreFile(new File(trustStorePath));
    }

    vaultConfig = vaultConfig.sslConfig(ssl.build());
    if (useTlsAuthentication) {
      Vault vaultDriver = new Vault(vaultConfig.build());
      vaultConfig = vaultConfig.token(vaultDriver.auth().loginByCert().getAuthClientToken());
    }

    return vaultConfig;
  }

  /**
   * Get VaultConfig from iam parameters
   * @param vaultConfig current state of the VaultConfig
   * @param iamParameters parameters from the iam element
   * @return VaultConfig with additional parameters added to it
   * @throws VaultException if there is an issue authenticating with IAM parameters
   */
  private VaultConfig getIamVaultConfig(VaultConfig vaultConfig, ConfigurationParameters iamParameters) throws VaultException {
    try {
      String authMount = iamParameters.getStringParameter("iamAwsAuthMount");
      String vaultRole = iamParameters.getStringParameter("iamVaultRole");
      String iamUrl = iamParameters.getStringParameter("iamUrl");
      String iamReqBody = iamParameters.getStringParameter("iamReqBody");
      String iamReqHeaders = iamParameters.getStringParameter("iamReqHeaders");

      String iamUrl_b64 = Base64.getEncoder().encodeToString(iamUrl.getBytes("UTF-8"));
      String iamReqBody_b64 = Base64.getEncoder().encodeToString(iamReqBody.getBytes("UTF-8"));
      String iamReqHeaders_b64 = Base64.getEncoder().encodeToString(iamReqHeaders.getBytes("UTF-8"));

      Vault vaultDriver = new Vault(vaultConfig.build());
      vaultConfig = vaultConfig.token(vaultDriver.auth().loginByAwsIam(vaultRole, iamUrl_b64, iamReqBody_b64, iamReqHeaders_b64, authMount).getAuthClientToken());

    } catch (VaultException ve) {
      throw ve;
    } catch (UnsupportedEncodingException uee) {
      LOGGER.error("Error encoding inputs", uee);
    } catch (Exception e) {
      LOGGER.debug("All IAM properties must be present (iamAwsAuthMount, iamVaultRole, iamUrl, iamReqBody, iamReqHeaders)");
    }
    return vaultConfig;
  }

  /**
   * Get VaultConfig from ec2 parameters
   *
   * If "useInstanceMetadata" is true, the PKCS7 value is retrieved from the AWS Metadata Service.
   * Else, the pkcs7 value is used if present
   * Otherwise, the identity document and identity signature are used
   *
   * @param vaultConfig current state of the VaultConfig
   * @param ec2Parameters parameters from the ec2 element
   * @return VaultConfig with additional parameters added to it
   * @throws VaultException if there is an issue authenticating with EC2 parameters
   */
  private VaultConfig getEc2VaultConfig(VaultConfig vaultConfig, ConfigurationParameters ec2Parameters) throws VaultException {

    String authMount = null;
    String vaultRole = null;
    boolean useInstanceMetadata = false;
    String pkcs7 = null;
    String identityDoc = null;
    String identityDocSignature = null;

    try {
      authMount = ec2Parameters.getStringParameter("ec2AwsAuthMount");
      vaultRole = ec2Parameters.getStringParameter("ec2VaultRole");
    } catch (Exception e) {
      LOGGER.debug("ec2AwsAuthMount and ec2VaultRole are required for EC2 authentication");
    }

    if (authMount != null && vaultRole != null && !authMount.isEmpty() && !vaultRole.isEmpty()) {
      try {
        String useInstMetaStr = ec2Parameters.getStringParameter("useInstanceMetadata");
        useInstanceMetadata = "true".equals(useInstMetaStr != null ? useInstMetaStr.toLowerCase() : "");
      } catch (Exception e) {
        LOGGER.debug("useInstanceMetadata is not present");
      }

      if (useInstanceMetadata) {
        pkcs7 = lookupPKCS7();
      } else {
        try {
          pkcs7 = ec2Parameters.getStringParameter("pkcs7");
        } catch (Exception e) {
          LOGGER.debug("pkcs7 property is not present");
        }
      }

      try {
        identityDoc = ec2Parameters.getStringParameter("identityDoc");
        identityDocSignature = ec2Parameters.getStringParameter("identityDocSignature");
      } catch (Exception ide) {
        LOGGER.debug("identityDoc and/or identityDocSignature properties are not present");
      }

      Vault vaultDriver = new Vault(vaultConfig.build());
      if (pkcs7 != null && !pkcs7.isEmpty()) {
        vaultConfig = vaultConfig
                .token(vaultDriver.auth().loginByAwsEc2(vaultRole,pkcs7, null, authMount).getAuthClientToken());
      } else if (identityDoc != null && identityDocSignature != null && !identityDoc.isEmpty() && !identityDocSignature.isEmpty()){
        vaultConfig = vaultConfig
                .token(vaultDriver.auth().loginByAwsEc2(vaultRole, identityDoc, identityDocSignature, null, authMount).getAuthClientToken());
      }
    }

    return vaultConfig;
  }

  /**
   * EC2 Provides a service to retrieve the instance identity
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

  private boolean classpathResourceExists(String path) {
    boolean fileExists = false;
    URL fileUrl = getClass().getResource(path);
    if (fileUrl != null) {
      File file = new File(fileUrl.getFile());
      if (file != null) {
        fileExists = file.exists();
      }
    }
    return fileExists;
  }

}
