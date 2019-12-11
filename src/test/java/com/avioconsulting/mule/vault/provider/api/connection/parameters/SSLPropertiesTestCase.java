package com.avioconsulting.mule.vault.provider.api.connection.parameters;

import junit.framework.TestCase;
import org.junit.Test;

public class SSLPropertiesTestCase extends TestCase {

    @Test
    public void testSSLProperties() {
        SSLProperties props = new SSLProperties();
        props.setPemFile("pemFile");
        props.setTrustStoreFile("trustStore");
        props.setVerifySSL(true);

        assertEquals("pemFile", props.getPemFile());
        assertEquals("trustStore", props.getTrustStoreFile());
        assertEquals(true, props.isVerifySSL());
    }
}
