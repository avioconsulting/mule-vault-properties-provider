package com.avioconsulting.mule.vault.provider.api.exception;

public class EmptyEnvironmentVariableException extends Exception {
    public EmptyEnvironmentVariableException(String errorMessage) {
        super(errorMessage);
    }
}
