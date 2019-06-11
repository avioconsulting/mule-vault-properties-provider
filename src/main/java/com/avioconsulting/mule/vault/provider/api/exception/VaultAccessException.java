package com.avioconsulting.mule.vault.provider.api.exception;

public class VaultAccessException extends Exception {
    public VaultAccessException(String errorMessage, Throwable cause) {
        super(errorMessage, cause);
    }
}
