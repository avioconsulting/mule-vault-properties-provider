package com.avioconsulting.mule.vault.provider.internal.connection.impl;

import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import org.mule.runtime.api.connection.ConnectionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.avioconsulting.mule.vault.provider.api.connection.parameters.TlsContext;
import io.github.jopenlibs.vault.SslConfig;
import io.github.jopenlibs.vault.Vault;
import io.github.jopenlibs.vault.VaultException;

import static com.avioconsulting.mule.vault.provider.internal.connection.provider.AbstractConnectionProvider.vaultConfig;

public class TlsConnection extends AbstractConnection {

  private static final Logger logger = LoggerFactory.getLogger(TlsConnection.class);

  public TlsConnection(TlsContext tlsContext) throws ConnectionException {

    try {
      SslConfig ssl = getVaultSSLConfig(tlsContext);
      vaultConfig = vaultConfig.sslConfig(ssl.build());
      logger.debug("TLS Setup Complete");
      Vault vaultDriver = new Vault(vaultConfig.build());
      String vaultToken = vaultDriver.auth().loginByCert().getAuthClientToken();
      this.vault = new Vault(vaultConfig.sslConfig(ssl.build()).token(vaultToken).build());
      this.valid = true;
    } catch (VaultException | CertificateException | NoSuchAlgorithmException | KeyStoreException
        | IOException ve) {
      throw new ConnectionException(ve);
    }
  }

}
