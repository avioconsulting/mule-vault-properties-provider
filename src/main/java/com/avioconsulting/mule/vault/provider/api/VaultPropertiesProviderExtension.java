package com.avioconsulting.mule.vault.provider.api;

import com.avioconsulting.mule.vault.provider.api.parameters.*;
import org.mule.runtime.extension.api.annotation.Configurations;
import org.mule.runtime.extension.api.annotation.Extension;
import org.mule.runtime.extension.api.annotation.SubTypeMapping;
import org.mule.runtime.extension.api.annotation.dsl.xml.Xml;

@Xml(prefix = "vault-properties-provider")
@Extension(name = "Vault Properties Provider")
@Configurations(VaultConfiguration.class)
@SubTypeMapping(baseType = VaultConfiguration.class,
                subTypes = {BasicVaultConfiguration.class, Ec2VaultConfiguration.class, IamVaultConfiguration.class, TlsVaultConfiguration.class})
public class VaultPropertiesProviderExtension {
}
