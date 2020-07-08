package com.avioconsulting.mule.vault.provider.internal.connection.parameters;

import com.avioconsulting.mule.vault.provider.api.connection.parameters.SSLProperties;
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
