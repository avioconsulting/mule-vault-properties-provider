package com.avioconsulting.mule.vault.provider.internal.extension;

/**
 * The path to a value in Vault. Contains the secret path and key name that
 * should be used to retreive the value from Vault.
 */
public class VaultPropertyPath {

  private String secretPath;

  private String key;

  public VaultPropertyPath() {
    super();
  }

  public VaultPropertyPath(String secretPath, String key) {
    super();
    this.secretPath = secretPath;
    this.key = key;
  }

  public String getSecretPath() {
    return secretPath;
  }

  public void setSecretPath(String secretPath) {
    this.secretPath = secretPath;
  }

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

}
