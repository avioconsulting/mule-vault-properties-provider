package com.avioconsulting.mule.vault.provider.api.connection.parameters;

import com.avioconsulting.mule.vault.provider.internal.extension.VaultPropertiesProviderExtension;
import org.mule.runtime.api.meta.ExpressionSupport;
import org.mule.runtime.config.api.dsl.model.ConfigurationParameters;
import org.mule.runtime.extension.api.annotation.Expression;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Summary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;

import static org.mule.runtime.api.component.ComponentIdentifier.builder;

/**
 * Properties to be used for AWS EC2 Connections with Identity and Signature
 */
public class AWSIdentityProperties {

  private static final Logger logger = LoggerFactory.getLogger(AWSIdentityProperties.class);

  private static final String IDENTITY_PARAMETER_GROUP = "identity-properties";

  @DisplayName("Identity Document")
  @Summary("Base64 encoded EC2 instance identity document.")
  @Parameter
  @Expression(ExpressionSupport.NOT_SUPPORTED)
  private String identity;

  @DisplayName("Identity Document Signature")
  @Summary("Base64 encoded SHA256 RSA signature of the instance identity document")
  @Parameter
  @Expression(ExpressionSupport.NOT_SUPPORTED)
  private String signature;

  public AWSIdentityProperties() {
    super();
  }

  /**
   * Construct an AWSIdentityProperties object with ConfigurationParameters
   * 
   * @param parameters
   *            {@link ConfigurationParameters} with a sub-element containing
   *            identity and signature
   */
  public AWSIdentityProperties(ConfigurationParameters parameters) {
    super();

    List<ConfigurationParameters> idList = parameters
        .getComplexConfigurationParameter(builder()
            .namespace(VaultPropertiesProviderExtension.EXTENSION_NAMESPACE)
            .name(IDENTITY_PARAMETER_GROUP).build());

    if (idList.size() > 0) {
      ConfigurationParameters idParameters = idList.get(0);

      try {
        identity = idParameters.getStringParameter("identity");
        signature = idParameters.getStringParameter("signature");
      } catch (Exception ide) {
        logger.debug("identity and/or signature properties are not present. If one is set, both must be set",
            ide);
      }
    }
  }

  public String getIdentity() {
    return identity;
  }

  public void setIdentity(String identity) {
    this.identity = identity;
  }

  public String getSignature() {
    return signature;
  }

  public void setSignature(String signature) {
    this.signature = signature;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    AWSIdentityProperties that = (AWSIdentityProperties) o;
    return Objects.equals(identity, that.identity) && Objects.equals(signature, that.signature);
  }

  @Override
  public int hashCode() {
    return Objects.hash(identity, signature);
  }
}
