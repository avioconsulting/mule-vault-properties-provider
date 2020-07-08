package com.avioconsulting.mule.vault.provider;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import com.avioconsulting.mule.vault.util.VaultContainer;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.mule.functional.junit4.MuleArtifactFunctionalTestCase;

import org.junit.Test;

import java.io.IOException;

public class VaultPropertiesProviderOperationsIT extends MuleArtifactFunctionalTestCase {

  @ClassRule
  public static final VaultContainer container = new VaultContainer();

  @BeforeClass
  public static void setupContainer() throws IOException, InterruptedException {
      container.initAndUnsealVault();
      container.enableKvSecretsV2();
      container.setupSampleSecret();

      // Set vaultUrl and vaultToken properties so they can be used in the Mule config file
      System.setProperty("vaultUrl", container.getAddress());
      System.setProperty("vaultToken", container.getRootToken());
      System.setProperty("pemFile", VaultContainer.CERT_PEMFILE);
      System.setProperty("ENV", "test");
  }

  /**
   * Specifies the mule config xml with the flows that are going to be executed in the tests, this file lives in the test
   * resources.
   */
  @Override
  protected String getConfigFile() {
    return "mule_config/test-mule-config-it.xml";
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

  @Test
  public void vaultPropertyProviderEnvValue() throws Exception {
    String payloadValue = ((String) flowRunner("envFlow")
        .run()
    .getMessage()
    .getPayload()
    .getValue());

    assertThat(payloadValue, is("test_value1"));
  }

}
