package com.avioconsulting.mule.vault.provider.internal.config;

import org.mule.runtime.extension.api.annotation.Configuration;
import org.mule.runtime.extension.api.annotation.connectivity.ConnectionProviders;

import com.avioconsulting.mule.vault.provider.internal.connection.provider.AppRoleConnectionProvider;
import com.avioconsulting.mule.vault.provider.internal.connection.provider.Ec2ConnectionProvider;
import com.avioconsulting.mule.vault.provider.internal.connection.provider.IamConnectionProvider;
import com.avioconsulting.mule.vault.provider.internal.connection.provider.TlsConnectionProvider;
import com.avioconsulting.mule.vault.provider.internal.connection.provider.TokenConnectionProvider;

@Configuration(name = "config")
@ConnectionProviders({ TokenConnectionProvider.class, TlsConnectionProvider.class, IamConnectionProvider.class,
    Ec2ConnectionProvider.class, AppRoleConnectionProvider.class })
public class VaultPropertiesProviderConfiguration {
}
