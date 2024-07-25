package com.avioconsulting.mule.vault.provider.internal.connection.parameters;

import com.avioconsulting.mule.vault.provider.api.connection.parameters.AWSIdentityProperties;
import com.avioconsulting.mule.vault.provider.api.connection.parameters.EC2ConnectionProperties;
import junit.framework.TestCase;
import org.junit.Test;

public class EC2PropertiesTestCase extends TestCase {

  @Test
  public void testSetters() {
    EC2ConnectionProperties props = new EC2ConnectionProperties();
    props.setUseInstanceMetadata(true);
    props.setPkcs7("testString");

    AWSIdentityProperties idProps = new AWSIdentityProperties();
    idProps.setIdentity("identity");
    idProps.setSignature("signature");

    assertEquals("identity", idProps.getIdentity());
    assertEquals("signature", idProps.getSignature());

    props.setIdentityProperties(idProps);

    assertEquals(idProps, props.getIdentityProperties());
    assertEquals("testString", props.getPkcs7());
    assertEquals(true, props.isUseInstanceMetadata());

  }
}
