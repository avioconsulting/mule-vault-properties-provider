package com.avioconsulting.mule.vault.provider.internal.connection.impl;

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

public class TokenConnection extends AbstractConnection {
  private static final Logger logger = LoggerFactory.getLogger(TokenConnection.class);

  public TokenConnection(String vaultToken, TlsContext tlsContext) throws ConnectionException {
    try {
      SslConfig ssl = getVaultSSLConfig(tlsContext);
      this.vault = new Vault(vaultConfig.token(vaultToken).sslConfig(ssl.build()).build());
      logger.debug("TLS Setup Complete");
      this.valid = true;
    } catch (VaultException | CertificateException | NoSuchAlgorithmException | KeyStoreException
        | IOException ve) {
      throw new ConnectionException(ve.getMessage(), ve.getCause());
    }
  }
}
