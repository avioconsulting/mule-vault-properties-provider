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
import org.mule.runtime.api.meta.model.declaration.fluent.ConfigurationDeclarer;
import org.mule.runtime.api.meta.model.declaration.fluent.ExtensionDeclarer;
import org.mule.runtime.api.meta.model.declaration.fluent.ParameterGroupDeclarer;
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

  @Override
  public void accept(ExtensionDeclarer extensionDeclarer, ExtensionLoadingContext context) {
    ConfigurationDeclarer configurationDeclarer = extensionDeclarer.named(EXTENSION_NAME)
        .describedAs(String.format("%s Extension", EXTENSION_NAME))
        .withCategory(SELECT)
        .onVersion("1.0.0")
        .fromVendor("AVIO Consulting, LLC")
        // This defines a global element in the extension with name config
        .withConfig(CONFIG_ELEMENT);

    ParameterGroupDeclarer defaultParameterGroup = configurationDeclarer.onDefaultParameterGroup();
    defaultParameterGroup
            .withRequiredParameter("vaultUrl").ofType(BaseTypeBuilder.create(JAVA).stringType().build())
            .withExpressionSupport(NOT_SUPPORTED)
            .describedAs("URL for the Vault Server");
    defaultParameterGroup
            .withOptionalParameter("vaultToken").ofType(BaseTypeBuilder.create(JAVA).stringType().build())
            .withExpressionSupport(NOT_SUPPORTED)
            .describedAs("Vault Token with access to necessary secrets");
    defaultParameterGroup
            .withOptionalParameter("verifySSL").ofType(BaseTypeBuilder.create(JAVA).booleanType().build())
            .withExpressionSupport(NOT_SUPPORTED)
            .describedAs("Should Vault SSL be verified");
    defaultParameterGroup
            .withOptionalParameter("keyStorePath").ofType(BaseTypeBuilder.create(JAVA).stringType().build())
            .withExpressionSupport(NOT_SUPPORTED)
            .describedAs("Path to the key store with the vault registered certificate");
    defaultParameterGroup
            .withOptionalParameter("keyStorePassword").ofType(BaseTypeBuilder.create(JAVA).stringType().build())
            .withExpressionSupport(NOT_SUPPORTED)
            .describedAs("Password for the key store");
    defaultParameterGroup
            .withOptionalParameter("trustStorePath").ofType(BaseTypeBuilder.create(JAVA).stringType().build())
            .withExpressionSupport(NOT_SUPPORTED)
            .describedAs("Path to the trust store for trusted certificates");
  }

}
