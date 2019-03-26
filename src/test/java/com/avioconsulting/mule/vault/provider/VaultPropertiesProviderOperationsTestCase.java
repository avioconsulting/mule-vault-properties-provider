package com.avioconsulting.mule.vault.provider;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import com.avioconsulting.mule.vault.util.VaultContainer;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.mule.functional.junit4.MuleArtifactFunctionalTestCase;

import javax.inject.Inject;

import org.junit.Test;

import java.io.IOException;

public class VaultPropertiesProviderOperationsTestCase extends MuleArtifactFunctionalTestCase {

  @ClassRule
  public static final VaultContainer container = new VaultContainer();

  @BeforeClass
  public static void setupContainer() throws IOException, InterruptedException {
      container.initAndUnsealVault();
      container.setupSampleSecret();

      // Set vaultUrl and vaultToken properties so they can be used in the Mule config file
      System.setProperty("vaultUrl", container.getAddress());
      System.setProperty("vaultToken", container.getRootToken());
  }

  /**
   * Specifies the mule config xml with the flows that are going to be executed in the tests, this file lives in the test
   * resources.
   */
  @Override
  protected String getConfigFile() {
    return "test-mule-config.xml";
  }

  @Test
  public void vaultPropertyProviderSuccessfullyConfigured() throws Exception {
    String payloadValue = ((String) flowRunner("testFlow")
            .run()
            .getMessage()
            .getPayload()
            .getValue());

    assertThat(payloadValue, is("test_value1"));
  }

}
