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
import java.util.Base64;

import static com.avioconsulting.mule.vault.provider.internal.connection.provider.AbstractConnectionProvider.vaultConfig;

public class IamConnection extends AbstractConnection {

  private static final String UTF_8 = "UTF-8";

  private static final Logger logger = LoggerFactory.getLogger(IamConnection.class);

  public IamConnection(String awsAuthMount, String role, String iamRequestUrl, String iamRequestBody,
      String iamRequestHeaders, TlsContext tlsContext) throws ConnectionException {

    try {
      SslConfig ssl = getVaultSSLConfig(tlsContext);
      vaultConfig.sslConfig(ssl.build());
      logger.debug("TLS Setup Complete");

      Vault vaultDriver = new Vault(vaultConfig.build());

      String requestUrl_b64 = Base64.getEncoder().encodeToString(iamRequestUrl.getBytes(UTF_8));
      String requestBody_b64 = Base64.getEncoder().encodeToString(iamRequestBody.getBytes(UTF_8));

      AuthResponse response = vaultDriver.auth().loginByAwsIam(role, requestUrl_b64, requestBody_b64,
          iamRequestHeaders, awsAuthMount);
      vaultConfig.token(response.getAuthClientToken());
      this.vault = new Vault(vaultConfig.build());
      this.valid = true;

    } catch (VaultException | CertificateException | NoSuchAlgorithmException | KeyStoreException | IOException e) {
      throw new ConnectionException(e);
    }
  }

}
