/*
 * (c) 2003-2018 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.avioconsulting.mule.vault.provider.api;

import static org.mule.metadata.api.model.MetadataFormat.JAVA;
import static org.mule.runtime.api.meta.Category.SELECT;
import static org.mule.runtime.api.meta.ExpressionSupport.NOT_SUPPORTED;

import org.mule.metadata.api.builder.BaseTypeBuilder;
import org.mule.runtime.api.meta.ExpressionSupport;
import org.mule.runtime.api.meta.model.declaration.fluent.*;
import org.mule.runtime.extension.api.loader.ExtensionLoadingContext;
import org.mule.runtime.extension.api.loader.ExtensionLoadingDelegate;

/**
 * Declares extension for Secure Properties Configuration module
 *
 * @since 1.0
 */
public class VaultConfigurationPropertiesExtensionLoadingDelegate implements ExtensionLoadingDelegate {

  public static final String EXTENSION_NAME = "Vault Properties Provider";
  public static final String CONFIG_ELEMENT = "config";

  public static final String BASIC_PARAMETER_GROUP = "basic";
  public static final String SSL_PARAMETER_GROUP = "ssl";
  public static final String IAM_PARAMETER_GROUP = "iam";
  public static final String EC2_PARAMETER_GROUP = "ec2";

  @Override
  public void accept(ExtensionDeclarer extensionDeclarer, ExtensionLoadingContext context) {

    ConfigurationDeclarer configurationDeclarer = extensionDeclarer.named(EXTENSION_NAME)
            .describedAs(String.format("%s Extension", EXTENSION_NAME))
            .withCategory(SELECT)
            .onVersion("1.0.0")
            .fromVendor("AVIO Consulting, LLC")
            .withConfig(CONFIG_ELEMENT);    // This defines a global element in the extension with name config

    ParameterGroupDeclarer defaultParameterGroup = configurationDeclarer.onDefaultParameterGroup();
    defaultParameterGroup
            .withRequiredParameter("vaultUrl").ofType(BaseTypeBuilder.create(JAVA).stringType().build())
            .withExpressionSupport(ExpressionSupport.SUPPORTED)
            .describedAs("URL for the Vault Server");

    addBasicParameters(configurationDeclarer);
    addSslParameters(configurationDeclarer);
    addIamParameters(configurationDeclarer);
    addEc2Parameters(configurationDeclarer);

  }

  private void addBasicParameters(ConfigurationDeclarer configurationDeclarer) {
    ParameterGroupDeclarer basicParameterGroup = configurationDeclarer
            .onParameterGroup(BASIC_PARAMETER_GROUP)
            .withDslInlineRepresentation(true);
    basicParameterGroup
            .withOptionalParameter("vaultToken").ofType(BaseTypeBuilder.create(JAVA).stringType().build())
            .withExpressionSupport(ExpressionSupport.SUPPORTED)
            .describedAs("Vault Token with access to necessary secrets");
    basicParameterGroup
            .withOptionalParameter("kvVersion").ofType(BaseTypeBuilder.create(JAVA).numberType().build())
            .withExpressionSupport(ExpressionSupport.SUPPORTED)
            .describedAs("KV Version Number");

  }

  private void addSslParameters(ConfigurationDeclarer configurationDeclarer) {
    ParameterGroupDeclarer sslParameterGroup = configurationDeclarer
            .onParameterGroup(SSL_PARAMETER_GROUP)
            .withDslInlineRepresentation(true);
    sslParameterGroup
            .withOptionalParameter("vaultPemFile").ofType(BaseTypeBuilder.create(JAVA).stringType().build())
            .withExpressionSupport(NOT_SUPPORTED)
            .describedAs("");
    sslParameterGroup
            .withOptionalParameter("keyStorePath").ofType(BaseTypeBuilder.create(JAVA).stringType().build())
            .withExpressionSupport(NOT_SUPPORTED)
            .describedAs("Path to the key store with the vault registered certificate");
    sslParameterGroup
            .withOptionalParameter("keyStorePassword").ofType(BaseTypeBuilder.create(JAVA).stringType().build())
            .withExpressionSupport(NOT_SUPPORTED)
            .describedAs("Password for the key store");
    sslParameterGroup
            .withOptionalParameter("trustStorePath").ofType(BaseTypeBuilder.create(JAVA).stringType().build())
            .withExpressionSupport(NOT_SUPPORTED)
            .describedAs("Path to the trust store for trusted certificates");
    sslParameterGroup
            .withOptionalParameter("useTlsAuth").ofType(BaseTypeBuilder.create(JAVA).booleanType().build())
            .withExpressionSupport(NOT_SUPPORTED)
            .describedAs("Use TLS Authentication")
            .defaultingTo(Boolean.FALSE);
    sslParameterGroup
            .withOptionalParameter("verifySSL").ofType(BaseTypeBuilder.create(JAVA).booleanType().build())
            .withExpressionSupport(NOT_SUPPORTED)
            .describedAs("Should Vault SSL be verified")
            .defaultingTo(Boolean.FALSE);
  }

  private void addIamParameters(ConfigurationDeclarer configurationDeclarer) {
    ParameterGroupDeclarer iamParameterGroup = configurationDeclarer
            .onParameterGroup(IAM_PARAMETER_GROUP)
            .withDslInlineRepresentation(true);
    iamParameterGroup
            .withOptionalParameter("iamAwsAuthMount").ofType(BaseTypeBuilder.create(JAVA).stringType().build())
            .withExpressionSupport(NOT_SUPPORTED)
            .describedAs("Mount point for AWS Authentication in Vault");
    iamParameterGroup
            .withOptionalParameter("iamVaultRole").ofType(BaseTypeBuilder.create(JAVA).stringType().build())
            .withExpressionSupport(NOT_SUPPORTED)
            .describedAs("Name of the role against which the login is being attempted. If role is not specified, then the login " +
                    "endpoint looks for a role bearing the name of the AMI ID of the EC2 instance that is trying to login if " +
                    "using the ec2 auth method, or the \"friendly name\" (i.e., role name or username) of the IAM principal " +
                    "authenticated. If a matching role is not found, login fails.");
    iamParameterGroup
            .withOptionalParameter("iamUrl").ofType(BaseTypeBuilder.create(JAVA).stringType().build())
            .withExpressionSupport(NOT_SUPPORTED)
            .describedAs("Most likely https://sts.amazonaws.com/")
            .defaultingTo("https://sts.amazonaws.com/");
    iamParameterGroup
            .withOptionalParameter("iamReqBody").ofType(BaseTypeBuilder.create(JAVA).stringType().build())
            .withExpressionSupport(NOT_SUPPORTED)
            .describedAs("Body of the signed request");
    iamParameterGroup
            .withOptionalParameter("iamReqHeaders").ofType(BaseTypeBuilder.create(JAVA).stringType().build())
            .withExpressionSupport(NOT_SUPPORTED)
            .describedAs("IAM Request Headers");
  }

  private void addEc2Parameters(ConfigurationDeclarer configurationDeclarer) {
    ParameterGroupDeclarer ec2ParameterGroup = configurationDeclarer
            .onParameterGroup(EC2_PARAMETER_GROUP)
            .withDslInlineRepresentation(true);
    ec2ParameterGroup
            .withOptionalParameter("ec2AwsAuthMount").ofType(BaseTypeBuilder.create(JAVA).stringType().build())
            .withExpressionSupport(NOT_SUPPORTED)
            .describedAs("Mount point for AWS Authentication in Vault");
    ec2ParameterGroup
            .withOptionalParameter("ec2VaultRole").ofType(BaseTypeBuilder.create(JAVA).stringType().build())
            .withExpressionSupport(NOT_SUPPORTED)
            .describedAs("");
    ec2ParameterGroup
            .withOptionalParameter("pkcs7").ofType(BaseTypeBuilder.create(JAVA).stringType().build())
            .withExpressionSupport(NOT_SUPPORTED)
            .describedAs("PKCS7 signature of the identity document with all \\n characters removed.");
    ec2ParameterGroup
            .withOptionalParameter("identityDoc").ofType(BaseTypeBuilder.create(JAVA).stringType().build())
            .withExpressionSupport(NOT_SUPPORTED)
            .describedAs("Base64 encoded EC2 instance identity document.");
    ec2ParameterGroup
            .withOptionalParameter("identityDocSignature").ofType(BaseTypeBuilder.create(JAVA).stringType().build())
            .withExpressionSupport(NOT_SUPPORTED)
            .describedAs("Base64 encoded SHA256 RSA signature of the instance identity document");
    ec2ParameterGroup
            .withOptionalParameter("useInstanceMetadata").ofType(BaseTypeBuilder.create(JAVA).booleanType().build())
            .withExpressionSupport(NOT_SUPPORTED)
            .describedAs("Retrieve Instance metadata")
            .defaultingTo(Boolean.FALSE);
  }

}
