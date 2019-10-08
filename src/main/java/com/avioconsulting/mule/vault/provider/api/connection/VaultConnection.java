package com.avioconsulting.mule.vault.provider.api.connection;

import com.bettercloud.vault.Vault;

public interface VaultConnection {

    Vault getVault();

    void invalidate();

    boolean isValid();

}
