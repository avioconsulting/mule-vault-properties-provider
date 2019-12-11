package com.avioconsulting.mule.vault.provider.api;

import org.mule.runtime.api.component.ComponentIdentifier;
import org.mule.runtime.api.meta.Category;
import org.mule.runtime.extension.api.annotation.Configurations;
import org.mule.runtime.extension.api.annotation.Export;
import org.mule.runtime.extension.api.annotation.Extension;
import org.mule.runtime.extension.api.annotation.dsl.xml.Xml;
import org.mule.runtime.extension.api.annotation.license.RequiresEnterpriseLicense;

import static org.mule.runtime.api.component.ComponentIdentifier.builder;

@Xml(prefix = "vault-properties-provider")
@Extension(name = "Vault Properties Provider", category = Category.CERTIFIED, vendor = "AVIO Consulting")
@RequiresEnterpriseLicense(allowEvaluationLicense = true)
@Configurations(VaultPropertiesProviderConfiguration.class)
@Export(classes = VaultConfigurationPropertiesProviderFactory.class,
        resources = "META-INF/services/org.mule.runtime.config.api.dsl.model.properties.ConfigurationPropertiesProviderFactory")
public class VaultPropertiesProviderExtension {
    public static final String EXTENSION_NAMESPACE = "vault-properties-provider";
    public static final ComponentIdentifier VAULT_PROPERTIES_PROVIDER =
            builder().namespace(EXTENSION_NAMESPACE).name("config").build();

    private VaultPropertiesProviderExtension() {
    }
}
