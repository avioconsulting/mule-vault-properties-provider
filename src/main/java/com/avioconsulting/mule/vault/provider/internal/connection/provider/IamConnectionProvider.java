package com.avioconsulting.mule.vault.provider.internal.connection.provider;

import com.avioconsulting.mule.vault.provider.api.connection.parameters.TlsContext;
import com.avioconsulting.mule.vault.provider.internal.connection.VaultConnection;
import com.avioconsulting.mule.vault.provider.internal.connection.impl.IamConnection;
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
import org.mule.runtime.extension.api.annotation.param.display.Summary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

@DisplayName("IAM Connection")
@Alias("iam-connection")
public class IamConnectionProvider extends AbstractAWSConnectionProvider {

    private static final Logger logger = LoggerFactory.getLogger(IamConnectionProvider.class);

    @DisplayName("IAM Request URL")
    @Summary("Most likely https://sts.amazonaws.com/")
    @Parameter
    @Optional(defaultValue = "https://sts.amazonaws.com/")
    private String iamRequestUrl = "https://sts.amazonaws.com/";

    @DisplayName("IAM Request Body")
    @Summary("Body of the signed request")
    @Parameter
    private String iamRequestBody;

    @DisplayName("IAM Request Headers")
    @Parameter
    private String iamRequestHeaders;

    @DisplayName("TLS Context")
    @Expression(ExpressionSupport.NOT_SUPPORTED)
    @ParameterDsl(allowReferences = false)
    @Placement(tab = "Security")
    @Parameter
    @Optional
    protected TlsContext tlsContext;

    public IamConnectionProvider() {
        super();
    }

    public IamConnectionProvider(ConfigurationParameters parameters) {
        super(parameters);

        tlsContext = new TlsContext(parameters);

        try {
            iamRequestUrl = parameters.getStringParameter("iamRequestUrl");
            iamRequestBody = parameters.getStringParameter("iamRequestBody");
            iamRequestHeaders = parameters.getStringParameter("iamRequestHeaders");
        } catch (Exception e) {
            logger.debug("All IAM properties must be present (iamAwsAuthMount, iamVaultRole, iamUrl, iamReqBody, iamReqHeaders)", e);
        }

    }

    @Override
    protected TlsContext getTlsContext() {
        return tlsContext;
    }

    @Override
    public VaultConnection connect() throws ConnectionException {
        return new IamConnection(vaultUrl, awsAuthMount, vaultRole, iamRequestUrl, iamRequestBody, iamRequestHeaders, getTlsContext(), engineVersion, prefixPathDepth);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IamConnectionProvider that = (IamConnectionProvider) o;
        return Objects.equals(iamRequestUrl, that.iamRequestUrl) && Objects.equals(iamRequestBody, that.iamRequestBody) && Objects.equals(iamRequestHeaders, that.iamRequestHeaders) && Objects.equals(tlsContext, that.tlsContext);
    }

    @Override
    public int hashCode() {
        return Objects.hash(iamRequestUrl, iamRequestBody, iamRequestHeaders, tlsContext);
    }
}
