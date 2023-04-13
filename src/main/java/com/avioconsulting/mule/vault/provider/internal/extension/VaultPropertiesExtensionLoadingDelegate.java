package com.avioconsulting.mule.vault.provider.internal.extension;

import org.mule.metadata.api.ClassTypeLoader;
import org.mule.metadata.api.builder.BaseTypeBuilder;
import org.mule.metadata.api.model.MetadataFormat;
import org.mule.runtime.api.meta.Category;
import org.mule.runtime.api.meta.ExpressionSupport;
import org.mule.runtime.api.meta.model.declaration.fluent.ConfigurationDeclarer;
import org.mule.runtime.api.meta.model.declaration.fluent.ExtensionDeclarer;
import org.mule.runtime.api.meta.model.declaration.fluent.ParameterGroupDeclarer;
import org.mule.runtime.api.meta.model.display.DisplayModel;
import org.mule.runtime.extension.api.declaration.type.ExtensionsTypeLoaderFactory;
import org.mule.runtime.extension.api.loader.ExtensionLoadingContext;
import org.mule.runtime.extension.api.loader.ExtensionLoadingDelegate;


public class VaultPropertiesExtensionLoadingDelegate implements ExtensionLoadingDelegate {

    public static final String EXTENSION_NAME = "HashiCorp Vault Properties Provider";
    public static final String CONFIG_ELEMENT = "config";
    public static final String VAULT_PARAMETER_GROUP = "Token Connection";

    @Override
    public void accept(ExtensionDeclarer extensionDeclarer, ExtensionLoadingContext extensionLoadingContext) {
        ConfigurationDeclarer configurationDeclarer = extensionDeclarer.named(EXTENSION_NAME)
            .describedAs(String.format("%s Extension", EXTENSION_NAME))
            .withCategory(Category.SELECT)
            .onVersion("1.0.0")
            .fromVendor("AVIO Consulting")
            .withConfig(CONFIG_ELEMENT);
        
            addVaultParameters(configurationDeclarer);
    }

    private void addVaultParameters(ConfigurationDeclarer configurationDeclarer) {
        final ParameterGroupDeclarer vaultParametersGroup = configurationDeclarer
            .onParameterGroup(VAULT_PARAMETER_GROUP)
            .withDslInlineRepresentation(true);

        ClassTypeLoader typeLoader = ExtensionsTypeLoaderFactory.getDefault().createTypeLoader();

        vaultParametersGroup
            .withRequiredParameter("vaultUrl")
            .withDisplayModel(DisplayModel.builder().displayName("Vault URL").build())
            .ofType(BaseTypeBuilder.create(MetadataFormat.JAVA).stringType().build())
            .withExpressionSupport(ExpressionSupport.SUPPORTED)
            .describedAs("Vault URL");

        vaultParametersGroup
            .withRequiredParameter("engineVersion")
            .withDisplayModel(DisplayModel.builder().displayName("Engine Version").build())
            .ofType(BaseTypeBuilder.create(MetadataFormat.JAVA).stringType().build())
            .withExpressionSupport(ExpressionSupport.SUPPORTED)
            .describedAs("Engine Version");

        vaultParametersGroup
            .withRequiredParameter("vaultToken")
            .withDisplayModel(DisplayModel.builder().displayName("Vault Token").build())
            .ofType(BaseTypeBuilder.create(MetadataFormat.JAVA).stringType().build())
            .withExpressionSupport(ExpressionSupport.SUPPORTED)
            .describedAs("Vault Token");
    }
}