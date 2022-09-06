package com.avioconsulting.mule.vault.provider.internal.connection.provider;

import com.avioconsulting.mule.vault.provider.api.connection.parameters.TlsContext;
import com.avioconsulting.mule.vault.provider.internal.connection.VaultConnection;
import com.avioconsulting.mule.vault.provider.internal.connection.impl.TokenConnection;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.meta.ExpressionSupport;
import org.mule.runtime.config.api.dsl.model.ConfigurationParameters;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.Expression;
import org.mule.runtime.extension.api.annotation.dsl.xml.ParameterDsl;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Placement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@DisplayName("Token Connection")
@Alias("token-connection")
public class TokenConnectionProvider extends AbstractConnectionProvider {

    private static final Logger logger = LoggerFactory.getLogger(TokenConnectionProvider.class);

    @DisplayName("Vault Token")
    @Parameter
    private String vaultToken;

    @DisplayName("TLS Context")
    @Expression(ExpressionSupport.NOT_SUPPORTED)
    @ParameterDsl(allowReferences = false)
    @Placement(tab = "Security")
    @Parameter
    @Optional
    protected TlsContext tlsContext;

    public TokenConnectionProvider() {
        super();
    }

    public TokenConnectionProvider(ConfigurationParameters parameters) {
        super(parameters);
        tlsContext = new TlsContext(parameters);

        try {
            vaultToken = parameters.getStringParameter("vaultToken");
        } catch (Exception e) {
            logger.debug("vaultToken parameter is not present", e);
        }
    }

    @Override
    protected TlsContext getTlsContext() {
        return tlsContext;
    }

    @Override
    public VaultConnection connect() throws ConnectionException {
        return new TokenConnection(vaultUrl, vaultToken, getTlsContext(), engineVersion, prefixPathDepth);
    }


}
