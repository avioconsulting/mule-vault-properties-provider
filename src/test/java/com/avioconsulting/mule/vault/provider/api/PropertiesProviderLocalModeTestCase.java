package com.avioconsulting.mule.vault.provider.api;

import org.junit.Test;
import org.mule.functional.junit4.MuleArtifactFunctionalTestCase;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class PropertiesProviderLocalModeTestCase extends MuleArtifactFunctionalTestCase {
    @Override
    protected String getConfigFile() {
        return "mule_config/test-mule-local-mode-config.xml";
    }
    @Test
    public void testEmptyEnvironmentVariable() throws Exception {
            String payloadValue = ((String) flowRunner("testFlow")
                    .run()
                    .getMessage()
                    .getPayload()
                    .getValue());
            assertThat(payloadValue, is("I-came-from-file"));
    }
}
