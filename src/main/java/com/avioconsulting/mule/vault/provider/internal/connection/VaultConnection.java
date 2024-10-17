package com.avioconsulting.mule.vault.provider.internal.connection;

import io.github.jopenlibs.vault.Vault;

public interface VaultConnection {

  Vault getVault();

  void invalidate();

  boolean isValid();

}
