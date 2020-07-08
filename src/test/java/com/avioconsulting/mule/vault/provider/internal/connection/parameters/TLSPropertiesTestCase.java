package com.avioconsulting.mule.vault.provider.internal.connection.parameters;

import com.avioconsulting.mule.vault.provider.api.connection.parameters.JKSProperties;
import com.avioconsulting.mule.vault.provider.api.connection.parameters.PEMProperties;
import com.avioconsulting.mule.vault.provider.api.connection.parameters.TLSAuthProperties;
import junit.framework.TestCase;
import org.junit.Test;

public class TLSPropertiesTestCase extends TestCase {

    @Test
    public void testTlsProps() {
        TLSAuthProperties props = new TLSAuthProperties();
        JKSProperties jksProps = new JKSProperties();
        PEMProperties pemProps = new PEMProperties();
        props.setJksProperties(jksProps);
        props.setPemProperties(pemProps);

        assertEquals(jksProps, props.getJksProperties());
        assertEquals(pemProps, props.getPemProperties());
    }
}
