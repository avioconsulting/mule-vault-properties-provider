package com.avioconsulting.mule.vault.provider.api.exception;

public class UnknownVaultException extends Exception {
    public UnknownVaultException(String errorMessage, Throwable cause) {
        super(errorMessage, cause);
    }
}
