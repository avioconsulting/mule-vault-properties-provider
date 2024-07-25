package com.avioconsulting.mule.vault.provider.internal.connection.parameters;

import com.avioconsulting.mule.vault.provider.api.connection.parameters.AWSIdentityProperties;
import junit.framework.TestCase;
import org.junit.Test;

public class AWSIdentityPropertiesTestCase extends TestCase {

  @Test
  public void testIamProperties() {
    AWSIdentityProperties props = new AWSIdentityProperties();
    props.setSignature("signature");
    props.setIdentity("identity");

    assertEquals("signature", props.getSignature());
    assertEquals("identity", props.getIdentity());
  }
}
