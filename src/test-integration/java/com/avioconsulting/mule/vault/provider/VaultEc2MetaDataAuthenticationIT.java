package com.avioconsulting.mule.vault.provider;

import com.avioconsulting.mule.vault.util.AwsCheck;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.mule.functional.junit4.MuleArtifactFunctionalTestCase;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assume.assumeTrue;

@Ignore
public class VaultEc2MetaDataAuthenticationIT extends MuleArtifactFunctionalTestCase {

    @BeforeClass
    public static void runCheckBeforeTest() {
        assumeTrue(AwsCheck.isAWSCheckEnabled() && AwsCheck.isExecutingOnAws());
    }

    @Override
    protected String getConfigFile() {
        return "mule_config/test-mule-ec2-metadata-auth-config.xml";
    }

    @Test
    public void testVaultEc2Authentication() throws Exception {
        assumeTrue(AwsCheck.isExecutingOnAws());
        String payloadValue = ((String) flowRunner("testFlow")
                .run()
                .getMessage()
                .getPayload()
                .getValue());

        assertThat(payloadValue, is("test_value1"));
    }

}