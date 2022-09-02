package com.avioconsulting.mule.vault.provider;

import com.avioconsulting.mule.vault.util.SSLUtils;
import com.avioconsulting.mule.vault.util.VaultContainer;
import org.bouncycastle.operator.OperatorCreationException;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.mule.functional.junit4.MuleArtifactFunctionalTestCase;

import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class VaultPrefixPathDepthIT extends MuleArtifactFunctionalTestCase {
    @ClassRule
    public static final VaultContainer container = new VaultContainer();

    @BeforeClass
    public static void setupContainer() throws IOException, InterruptedException, CertificateException, NoSuchAlgorithmException, KeyStoreException, SignatureException, NoSuchProviderException, InvalidKeyException, OperatorCreationException {
        container.initAndUnsealVault();
        SSLUtils.createClientCertAndKey();
        container.setupSampleSecretDepth();

        // Set vaultUrl and vaultToken properties so they can be used in the Mule config file
        System.setProperty("vaultUrl", container.getAddress());
        System.setProperty("vaultToken", container.getRootToken());
        System.setProperty("trustStoreFile", VaultContainer.CLIENT_TRUSTSTORE);
        System.setProperty("trustStorePassword", "password");
    }

    protected String getConfigFile() {
        return "mule_config/test-mule-prefix-path-depth-config-it.xml";
    }

    @Test
    public void vaultPrefixPathDepthSuccessfullyConfigured() throws Exception {
        String payloadValue = ((String) flowRunner("testFlow")
                .run()
                .getMessage()
                .getPayload()
                .getValue());

        String payloadValue2 = ((String) flowRunner("testFlow2")
                .run()
                .getMessage()
                .getPayload()
                .getValue());

        assertThat(payloadValue, is("test_value1"));
        assertThat(payloadValue2, is("test_value2"));
    }

}
