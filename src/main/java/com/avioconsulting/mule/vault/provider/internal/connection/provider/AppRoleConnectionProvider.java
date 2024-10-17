package com.avioconsulting.mule.vault.provider.internal.connection.provider;

import com.avioconsulting.mule.vault.provider.api.connection.parameters.TlsContext;
import com.avioconsulting.mule.vault.provider.internal.connection.VaultConnection;
import com.avioconsulting.mule.vault.provider.internal.connection.impl.AppRoleConnection;
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

@DisplayName("AppRole Connection")
@Alias("approle-connection")
public class AppRoleConnectionProvider extends AbstractConnectionProvider {
  private static final Logger logger = LoggerFactory.getLogger(AppRoleConnectionProvider.class);

  @DisplayName("AppRole Mount")
  @Summary("Mount point for AppRole Authentication in Vault")
  @Parameter
  @Optional(defaultValue = "approle")
  private String authMount;

  @DisplayName("Vault Role Id")
  @Parameter
  private String roleId;

  @DisplayName("Vault Secret Id")
  @Parameter
  private String secretId;

  @DisplayName("TLS Context")
  @Expression(ExpressionSupport.NOT_SUPPORTED)
  @ParameterDsl(allowReferences = false)
  @Placement(tab = "Security")
  @Parameter
  @Optional
  protected TlsContext tlsContext;

  public AppRoleConnectionProvider() {
    super();
  }

  public AppRoleConnectionProvider(ConfigurationParameters parameters) {
    super(parameters);
    tlsContext = new TlsContext(parameters);
    try {
      roleId = parameters.getStringParameter("roleId");
      secretId = parameters.getStringParameter("secretId");
      authMount = parameters.getStringParameter("authMount");
    } catch (Exception e) {
      logger.debug("Role Id or Secret Id is not present", e);
    }
  }

  @Override
  protected TlsContext getTlsContext() {
    return tlsContext;
  }

  @Override
  public VaultConnection connect() throws ConnectionException {
    return new AppRoleConnection(authMount, roleId, secretId, getTlsContext());
  }
}
