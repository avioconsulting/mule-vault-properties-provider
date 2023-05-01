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

public class VaultEmptyTlsFieldIT extends MuleArtifactFunctionalTestCase {
    @ClassRule
    public static final VaultContainer container = new VaultContainer();

    @BeforeClass
    public static void setupContainer() throws IOException, InterruptedException, CertificateException, KeyStoreException, NoSuchAlgorithmException, SignatureException, NoSuchProviderException, InvalidKeyException, OperatorCreationException {
        container.initAndUnsealVault();
        SSLUtils.createClientCertAndKey();
        container.setupBackendCert();
        container.enableKvSecretsV2();
        container.setupSampleSecret();

        // Set vaultUrl and vaultToken properties so they can be used in the Mule config file
        System.setProperty("vaultUrl", container.getAddress());
        System.setProperty("trustStoreFile", VaultContainer.CLIENT_TRUSTSTORE);
        System.setProperty("trustStorePassword", "password");
        System.setProperty("vaultToken", container.getRootToken());
    }

    @Override
    protected String getConfigFile() {
        return "mule_config/test-mule-empty-field-config-it.xml";
    }

    @Test
    public void testVaultAppRoleAuthentication() throws Exception {
        String payloadValue = ((String) flowRunner("vault-properties-demoFlow")
                .run()
                .getMessage()
                .getPayload()
                .getValue());

        assertThat(payloadValue, is("test_value1"));
    }
}
