package com.avioconsulting.mule.vault.provider.api.connection.parameters;

import org.mule.runtime.config.api.dsl.model.ConfigurationParameters;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.Password;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Objects;

public class KeyStoreConfig {

  private static final Logger logger = LoggerFactory.getLogger(KeyStoreConfig.class);
  @Parameter
  @Optional(defaultValue = "JKS")
  private String type;

  @Parameter
  @Optional
  private String path;

  @Parameter
  @Password
  @Optional
  private String password;

  public KeyStoreConfig() {
    super();
  }

  public KeyStoreConfig(ConfigurationParameters parameters) {
    super();

    try {
      type = parameters.getStringParameter("type");
    } catch (Exception e) {
      logger.debug("TrustStore type is not set. Using JKS.");
      type = "JKS";
    }

    try {
      path = parameters.getStringParameter("path");
      password = parameters.getStringParameter("password");
    } catch (Exception e) {
      logger.error(
          "Both path and password are required in the trust-store configuration, if one is set, the other must be as well",
          e);
      throw e;
    }
  }

  public KeyStore getKeyStore()
      throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException {
    KeyStore ks = KeyStore.getInstance(type);
    ks.load(new FileInputStream(path), password.toCharArray());
    logger.debug(String.format("Using keystore from %s", path));
    return ks;
  }

  public String getPassword() {
    return password;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    KeyStoreConfig that = (KeyStoreConfig) o;
    return type.equals(that.type) && path.equals(that.path) && Objects.equals(password, that.password);
  }

  @Override
  public int hashCode() {
    return Objects.hash(type, path, password);
  }
}
