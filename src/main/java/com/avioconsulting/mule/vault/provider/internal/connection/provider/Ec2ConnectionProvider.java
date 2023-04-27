package com.avioconsulting.mule.vault.provider.internal.connection.provider;

import com.avioconsulting.mule.vault.provider.api.connection.parameters.EC2ConnectionProperties;
import com.avioconsulting.mule.vault.provider.api.connection.parameters.TlsContext;
import com.avioconsulting.mule.vault.provider.internal.connection.VaultConnection;
import com.avioconsulting.mule.vault.provider.internal.connection.impl.Ec2Connection;
import com.intellectualsites.http.HttpClient;
import com.intellectualsites.http.HttpResponse;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.meta.ExpressionSupport;
import org.mule.runtime.config.api.dsl.model.ConfigurationParameters;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.Expression;
import org.mule.runtime.extension.api.annotation.dsl.xml.ParameterDsl;
import org.mule.runtime.extension.api.annotation.param.ExclusiveOptionals;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.ParameterGroup;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Placement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

@DisplayName("EC2 Connection")
@Alias("ec2-connection")
@ExclusiveOptionals(isOneRequired = true)
public class Ec2ConnectionProvider extends AbstractAWSConnectionProvider {

	// This is the URI to use to retrieve the PKCS7 Signature
	// See: https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/instance-identity-documents.html
	private static final String INSTANCE_PKCS7_URI = "http://169.254.169.254/latest/dynamic/instance-identity/pkcs7";
	private static final Logger logger = LoggerFactory.getLogger(Ec2ConnectionProvider.class);
	@DisplayName("TLS Context")
	@Expression(ExpressionSupport.NOT_SUPPORTED)
	@ParameterDsl(allowReferences = false)
	@Placement(tab = "Security")
	@Parameter
	@Optional
	protected TlsContext tlsContext;
	@ParameterGroup(name = "EC2 Properties")
	EC2ConnectionProperties connectionProperties;
	private String pkcs7Uri;

	public Ec2ConnectionProvider() {
		super();
		setPkcs7Uri();
	}

	private void setPkcs7Uri() {
		pkcs7Uri = System.getProperty("INSTANCE_PKCS7_URI");
		if (pkcs7Uri == null || pkcs7Uri.isEmpty()) {
			pkcs7Uri = INSTANCE_PKCS7_URI;
		}
	}

	public Ec2ConnectionProvider(ConfigurationParameters parameters) {
		super(parameters);
		setPkcs7Uri();
		connectionProperties = new EC2ConnectionProperties(parameters);
		tlsContext = new TlsContext(parameters);
	}

	@Override
	public VaultConnection connect() throws ConnectionException {
		if (connectionProperties != null) {
			if (connectionProperties.isUseInstanceMetadata()) {
				connectionProperties.setPkcs7(lookupPkcs7());
			}
			boolean pkcsUnavailable =
					connectionProperties.getPkcs7() == null || connectionProperties.getPkcs7().isEmpty();
			boolean identityUnavailable = connectionProperties.getIdentityProperties().getIdentity() == null
					|| connectionProperties.getIdentityProperties().getIdentity().isEmpty()
					|| connectionProperties.getIdentityProperties().getSignature() == null
					|| connectionProperties.getIdentityProperties().getSignature().isEmpty();

			if (pkcsUnavailable && identityUnavailable) {
				logger.error("PKCS7 Signature, Identity Document, and Identity Signature are all null or empty");
				throw new ConnectionException("PKCS7 Signature or the Identity Document and Signature are required");
			}

			return new Ec2Connection(vaultUrl,
					vaultRole,
					connectionProperties.getPkcs7(),
					null,
					connectionProperties.getIdentityProperties().getIdentity(),
					connectionProperties.getIdentityProperties().getSignature(),
					awsAuthMount,
					getTlsContext(),
					engineVersion,
					prefixPathDepth);
		} else {
			return null;
		}
	}

	/**
	 * EC2 Provides a service to retrieve the instance identity. This method uses that service to look up the PKCS7.
	 *
	 * @return the PKCS7 value with the '\n' characters removed
	 */
	private String lookupPkcs7() {
		String pkcs7 = null;
		HttpClient.Builder client = HttpClient.newBuilder();
		HttpResponse response = client.build().get(pkcs7Uri).execute();
		String responseStr = response.getResponseEntity(String.class);
		pkcs7 = responseStr.replaceAll("\n", "");
		return pkcs7;
	}

	@Override
	protected TlsContext getTlsContext() {
		return tlsContext;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Ec2ConnectionProvider that = (Ec2ConnectionProvider) o;
		return Objects.equals(tlsContext, that.tlsContext) && Objects.equals(connectionProperties, that.connectionProperties) && Objects.equals(pkcs7Uri, that.pkcs7Uri);
	}

	@Override
	public int hashCode() {
		return Objects.hash(tlsContext, connectionProperties, pkcs7Uri);
	}
}
