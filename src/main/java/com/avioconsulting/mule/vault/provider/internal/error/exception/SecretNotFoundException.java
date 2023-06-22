package com.avioconsulting.mule.vault.provider.internal.error.exception;

import com.avioconsulting.mule.vault.provider.internal.error.VaultErrorType;
import org.mule.runtime.extension.api.exception.ModuleException;

public class SecretNotFoundException extends ModuleException {
    public SecretNotFoundException(Exception cause) {
        super(VaultErrorType.SECRET_NOT_FOUND, cause);
    }
    public SecretNotFoundException(String message, Throwable cause) {
        super(message, VaultErrorType.SECRET_NOT_FOUND, cause);
    }
    public SecretNotFoundException(String message) {
        super(message, VaultErrorType.SECRET_NOT_FOUND);
    }
}
