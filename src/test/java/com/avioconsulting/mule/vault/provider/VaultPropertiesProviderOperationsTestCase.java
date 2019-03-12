package com.avioconsulting.mule.vault.provider;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import org.mule.functional.junit4.MuleArtifactFunctionalTestCase;

import javax.inject.Inject;

import org.junit.Test;

public class VaultPropertiesProviderOperationsTestCase extends MuleArtifactFunctionalTestCase {

  /**
   * Specifies the mule config xml with the flows that are going to be executed in the tests, this file lives in the test
   * resources.
   */
  @Override
  protected String getConfigFile() {
    return "test-mule-config.xml";
  }

  @Inject
  private TestObject testObject;

  @Test
  public void vaultPropertyProviderSuccessfullyConfigured() {
    assertThat(testObject.getValueFromProperty(), is("mule-293@vault-sample-231716.iam.gserviceaccount.com"));
  }

}
