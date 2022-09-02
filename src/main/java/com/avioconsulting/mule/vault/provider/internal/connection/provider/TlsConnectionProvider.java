package com.avioconsulting.mule.vault.provider.internal.connection.provider;

import com.avioconsulting.mule.vault.provider.api.connection.parameters.TlsContext;
import com.avioconsulting.mule.vault.provider.internal.connection.VaultConnection;
import com.avioconsulting.mule.vault.provider.internal.connection.impl.TlsConnection;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.meta.ExpressionSupport;
import org.mule.runtime.config.api.dsl.model.ConfigurationParameters;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.Expression;
import org.mule.runtime.extension.api.annotation.dsl.xml.ParameterDsl;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@DisplayName("TLS Connection")
@Alias("tls-connection")
public class TlsConnectionProvider extends AbstractConnectionProvider {

    private static final Logger logger = LoggerFactory.getLogger(TlsConnectionProvider.class);

    @DisplayName("TLS Context")
    @Expression(ExpressionSupport.NOT_SUPPORTED)
    @ParameterDsl(allowReferences = false)
    @Parameter
    protected TlsContext tlsContext;

    public TlsConnectionProvider() {
        super();
    }

    public TlsConnectionProvider(ConfigurationParameters parameters) {
        super(parameters);
        tlsContext = new TlsContext(parameters);
    }

    @Override
    protected TlsContext getTlsContext() {
        return tlsContext;
    }

    @Override
    public VaultConnection connect() throws ConnectionException {
        if (tlsContext == null) {
            throw new ConnectionException("TLS Context is required for TLS Connection");
        }
        return new TlsConnection(vaultUrl, getTlsContext(), engineVersion, prefixPathDepth);
    }

}
