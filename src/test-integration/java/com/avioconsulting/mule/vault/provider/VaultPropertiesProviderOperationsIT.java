package com.avioconsulting.mule.vault.provider;

import com.avioconsulting.mule.vault.util.SSLUtils;
import com.avioconsulting.mule.vault.util.VaultContainer;
import org.bouncycastle.operator.OperatorCreationException;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.mule.functional.junit4.MuleArtifactFunctionalTestCase;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.cert.CertificateException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class VaultPropertiesProviderOperationsIT extends MuleArtifactFunctionalTestCase {

  @ClassRule
  public static final VaultContainer container = new VaultContainer();

  @BeforeClass
  public static void setupContainer() throws IOException, InterruptedException, CertificateException, NoSuchAlgorithmException, KeyStoreException, SignatureException, NoSuchProviderException, InvalidKeyException, OperatorCreationException {
      container.initAndUnsealVault();
      SSLUtils.createClientCertAndKey();
      container.enableKvSecretsV2();
      container.setupSampleSecret();

      // Set vaultUrl and vaultToken properties so they can be used in the Mule config file
      System.setProperty("vaultUrl", container.getAddress());
      System.setProperty("vaultToken", container.getRootToken());
      System.setProperty("trustStoreFile", VaultContainer.CLIENT_TRUSTSTORE);
      System.setProperty("trustStorePassword", "password");
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
