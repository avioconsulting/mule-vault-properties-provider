package com.avioconsulting.mule.vault.provider;

import com.avioconsulting.mule.vault.util.AwsCheck;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mule.functional.junit4.MuleArtifactFunctionalTestCase;

import static org.junit.Assume.assumeTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class VaultIamAuthenticationIT extends MuleArtifactFunctionalTestCase {

    @BeforeClass
    public static void runCheckBeforeTest() {
        assumeTrue(AwsCheck.isAWSCheckEnabled() && AwsCheck.isExecutingOnAws());
    }

    @Override
    protected String getConfigFile() {
        return "mule_config/test-mule-iam-auth-config-it.xml";
    }

    @Test
    public void testVaultIAMAuthentication() throws Exception {
        assumeTrue(AwsCheck.isExecutingOnAws());
        String payloadValue = ((String) flowRunner("testFlow")
                .run()
                .getMessage()
                .getPayload()
                .getValue());

        assertThat(payloadValue, is("test_value1"));
    }

}
