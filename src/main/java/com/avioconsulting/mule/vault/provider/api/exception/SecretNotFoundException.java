package com.avioconsulting.mule.vault.provider.api.exception;

public class SecretNotFoundException extends Exception {
    public SecretNotFoundException(String errorMessage, Throwable cause) {
        super(errorMessage, cause);
    }
}
