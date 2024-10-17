package com.avioconsulting.mule.vault.provider.internal.connection.impl;

import com.avioconsulting.mule.vault.provider.api.connection.parameters.EngineVersion;
import com.avioconsulting.mule.vault.provider.api.connection.parameters.TlsContext;
import io.github.jopenlibs.vault.SslConfig;
import io.github.jopenlibs.vault.Vault;
import io.github.jopenlibs.vault.VaultException;
import org.mule.runtime.api.connection.ConnectionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import static com.avioconsulting.mule.vault.provider.internal.connection.provider.AbstractConnectionProvider.vaultConfig;

public class AppRoleConnection extends AbstractConnection {
  private static final Logger logger = LoggerFactory.getLogger(AppRoleConnection.class);

  public AppRoleConnection(String authMount, String roleId, String secretId, TlsContext tlsContext)
      throws ConnectionException {

    try {
      SslConfig ssl = getVaultSSLConfig(tlsContext);
      vaultConfig = vaultConfig.sslConfig(ssl.build());
      vault = new Vault(vaultConfig.build());
      String token = vault.auth().loginByAppRole(authMount, roleId, secretId).getAuthClientToken();
      this.vault = new Vault(vaultConfig.sslConfig(ssl.build()).token(token).build());
      logger.debug("Successfully authenticated with AppRole auth method");
      this.valid = true;
    } catch (VaultException | CertificateException | NoSuchAlgorithmException | KeyStoreException
        | IOException ve) {
      logger.error("Error trying to stablish approle connection", ve);
      throw new ConnectionException(ve.getMessage(), ve.getCause());
    }
  }
}
