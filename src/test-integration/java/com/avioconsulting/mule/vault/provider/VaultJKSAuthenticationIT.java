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

public class VaultJKSAuthenticationIT extends MuleArtifactFunctionalTestCase {

    @ClassRule
    public static final VaultContainer container = new VaultContainer();

    @BeforeClass
    public static void setupContainer() throws IOException, InterruptedException, CertificateException,
            SignatureException, NoSuchAlgorithmException, KeyStoreException, OperatorCreationException,
            NoSuchProviderException, InvalidKeyException {
        container.initAndUnsealVault();
        SSLUtils.createClientCertAndKey();
        container.setupBackendCert();
        container.enableKvSecretsV2();
        container.setupSampleSecret();

        // Set vaultUrl and vaultToken properties so they can be used in the Mule config file
        System.setProperty("vaultUrl", container.getAddress());
        System.setProperty("keyStorePath", VaultContainer.CLIENT_KEYSTORE);
        System.setProperty("keyStorePassword", "password");
        System.setProperty("trustStoreFile", VaultContainer.CLIENT_TRUSTSTORE);
        System.setProperty("trustStorePassword", "password");
    }

    @Override
    protected String getConfigFile() {
        return "mule_config/test-mule-jks-auth-config-it.xml";
    }

    @Test
    public void testVaultTLSAuthentication() throws Exception {
        String payloadValue = ((String) flowRunner("testFlow")
                .run()
                .getMessage()
                .getPayload()
                .getValue());

        assertThat(payloadValue, is("test_value1"));
    }
}