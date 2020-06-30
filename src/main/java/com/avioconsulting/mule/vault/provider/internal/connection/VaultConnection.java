package com.avioconsulting.mule.vault.provider.internal.connection;

import com.bettercloud.vault.Vault;

public interface VaultConnection {

    Vault getVault();

    void invalidate();

    boolean isValid();

}
