package com.avioconsulting.mule.vault.provider.api.connection.parameters;

/**
 * Enumeration of the acceptable secrets engine versions
 */
public enum EngineVersion {
    v2 (2),
    v1 (1);


    private final Integer engineVersion;

    EngineVersion(Integer i) {
        this.engineVersion = i;
    }

    public Integer getEngineVersionNumber() {
        return this.engineVersion;
    }
}