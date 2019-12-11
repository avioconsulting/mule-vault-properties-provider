package com.avioconsulting.mule.vault.provider.api.connection.parameters;

import junit.framework.TestCase;
import org.junit.Test;

public class PEMPropertiesTestCase extends TestCase {

    @Test
    public void testGettersAndSetters() {
        PEMProperties props = new PEMProperties();

        props.setClientKeyPemFile("key");
        props.setClientPemFile("file");

        assertEquals("file", props.getClientPemFile());
        assertEquals("key", props.getClientKeyPemFile());
    }
}
