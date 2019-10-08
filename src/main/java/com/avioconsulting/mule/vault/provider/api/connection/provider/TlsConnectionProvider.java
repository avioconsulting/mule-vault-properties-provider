package com.avioconsulting.mule.vault.provider.api.connection.provider;

import com.avioconsulting.mule.vault.provider.api.connection.VaultConnection;
import com.avioconsulting.mule.vault.provider.api.connection.impl.TlsConnection;
import com.avioconsulting.mule.vault.provider.api.connection.parameters.TLSAuthProperties;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.connection.ConnectionValidationResult;
import org.mule.runtime.config.api.dsl.model.ConfigurationParameters;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.param.ParameterGroup;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;

@DisplayName("TLS Connection")
@Alias("tls-connection")
public class TlsConnectionProvider extends AbstractConnectionProvider {

    @ParameterGroup(name="TLS Authentication Parameters")
    private TLSAuthProperties tlsAuthProperties;

    public TlsConnectionProvider() {
        super();
    }

    public TlsConnectionProvider(ConfigurationParameters parameters) {
        super(parameters);

        tlsAuthProperties = new TLSAuthProperties(parameters);
    }

    @Override
    public VaultConnection connect() throws ConnectionException {
        return new TlsConnection(vaultUrl, tlsAuthProperties.getJksProperties(), tlsAuthProperties.getPemProperties(), sslProperties, engineVersion);
    }

    @Override
    public void disconnect(VaultConnection vaultConnection) {
        vaultConnection.invalidate();
    }

    @Override
    public ConnectionValidationResult validate(VaultConnection vaultConnection) {
        if (vaultConnection.isValid()) {
            return ConnectionValidationResult.success();
        } else {
            return ConnectionValidationResult.failure("Connection Invalid", null);
        }
    }
}
