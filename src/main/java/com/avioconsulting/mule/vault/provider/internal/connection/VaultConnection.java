package com.avioconsulting.mule.vault.provider.internal.connection;

import com.avioconsulting.vault.http.client.provider.VaultClient;

public interface VaultConnection {

    VaultClient getVaultClient();

    void invalidate();

    boolean isValid();

}
