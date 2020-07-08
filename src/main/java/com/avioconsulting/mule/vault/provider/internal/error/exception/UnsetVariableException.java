package com.avioconsulting.mule.vault.provider.internal.error.exception;

import com.avioconsulting.mule.vault.provider.internal.error.VaultErrorType;
import org.mule.runtime.extension.api.exception.ModuleException;

public class UnsetVariableException extends ModuleException {
    public UnsetVariableException(Exception cause) {
        super(VaultErrorType.UNSET_VARIABLE, cause);
    }
    public UnsetVariableException(String message, Throwable cause) {
        super(message, VaultErrorType.UNSET_VARIABLE, cause);
    }
    public UnsetVariableException(String message) {
        super(message, VaultErrorType.UNSET_VARIABLE);
    }
}
