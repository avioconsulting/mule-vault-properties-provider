package com.avioconsulting.mule.vault.provider.internal.connection.impl;

import com.avioconsulting.mule.vault.provider.api.connection.parameters.TlsContext;
import io.github.jopenlibs.vault.SslConfig;
import io.github.jopenlibs.vault.Vault;
import io.github.jopenlibs.vault.VaultException;
import io.github.jopenlibs.vault.response.AuthResponse;
import org.mule.runtime.api.connection.ConnectionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import static com.avioconsulting.mule.vault.provider.internal.connection.provider.AbstractConnectionProvider.vaultConfig;

public class Ec2Connection extends AbstractConnection {

  private static final Logger logger = LoggerFactory.getLogger(Ec2Connection.class);

  public Ec2Connection(String role, String pkcs7, String nonce, String identity, String signature,
      String awsAuthMount, TlsContext tlsContext) throws ConnectionException {
    try {
      SslConfig ssl = getVaultSSLConfig(tlsContext);
      vaultConfig.sslConfig(ssl.build());
      logger.debug("TLS Setup Complete");

      Vault vaultDriver = new Vault(vaultConfig.build());
      AuthResponse response = null;
      if (pkcs7 != null && !pkcs7.isEmpty()) {
        response = vaultDriver.auth().loginByAwsEc2(role, pkcs7, nonce, awsAuthMount);
      } else {
        response = vaultDriver.auth().loginByAwsEc2(role, identity, signature, nonce, awsAuthMount);
      }
      vaultConfig.token(response.getAuthClientToken());
      this.vault = new Vault(vaultConfig.build());
      this.valid = true;
    } catch (VaultException | IOException | CertificateException | NoSuchAlgorithmException | KeyStoreException e) {
      throw new ConnectionException(e);
    }
  }
}
