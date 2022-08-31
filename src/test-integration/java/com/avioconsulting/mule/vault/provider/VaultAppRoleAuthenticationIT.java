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

public class VaultAppRoleAuthenticationIT extends MuleArtifactFunctionalTestCase {
    @ClassRule
    public static final VaultContainer container = new VaultContainer();

    @BeforeClass
    public static void setupContainer() throws IOException, InterruptedException, CertificateException, KeyStoreException, NoSuchAlgorithmException, SignatureException, NoSuchProviderException, InvalidKeyException, OperatorCreationException {
        container.initAndUnsealVault();
        SSLUtils.createClientCertAndKey();
        container.setupBackendCert();
        container.addAndConfigureAppRole();
        container.enableKvSecretsV2();
        container.setupSampleSecret();

        // Set vaultUrl and vaultToken properties so they can be used in the Mule config file
        System.setProperty("vaultUrl", container.getAddress());
        System.setProperty("trustStoreFile", VaultContainer.CLIENT_TRUSTSTORE);
        System.setProperty("trustStorePassword", "password");
        System.setProperty("role_id", container.getRoleId());
        System.setProperty("secret_id", container.getSecretId());
    }

    @Override
    protected String getConfigFile() {
        return "mule_config/test-mule-app-role-auth-config-it.xml";
    }

    @Test
    public void testVaultAppRoleAuthentication() throws Exception {
        String payloadValue = ((String) flowRunner("testFlow")
                .run()
                .getMessage()
                .getPayload()
                .getValue());

        assertThat(payloadValue, is("test_value1"));
    }
}
