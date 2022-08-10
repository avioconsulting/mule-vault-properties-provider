package com.avioconsulting.mule.vault.provider.internal.config;

import com.avioconsulting.mule.vault.provider.internal.connection.provider.*;
import org.mule.runtime.extension.api.annotation.Configuration;
import org.mule.runtime.extension.api.annotation.connectivity.ConnectionProviders;

@Configuration(name="config")
@ConnectionProviders({TokenConnectionProvider.class, TlsConnectionProvider.class, IamConnectionProvider.class, Ec2ConnectionProvider.class})
public class VaultPropertiesProviderConfiguration {
}
