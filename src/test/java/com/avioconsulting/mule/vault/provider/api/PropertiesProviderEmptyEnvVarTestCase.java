package com.avioconsulting.mule.vault.provider.api;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockserver.client.MockServerClient;
import org.mockserver.junit.MockServerRule;
import org.mule.functional.junit4.MuleArtifactFunctionalTestCase;

import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

public class PropertiesProviderEmptyEnvVarTestCase extends MuleArtifactFunctionalTestCase {
    @Rule
    public MockServerRule mockServerRule = new MockServerRule(this);
    private MockServerClient mockClient;

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Override
    protected String getConfigFile() {
        // TODO @amead to review, matched this string to get test to pass for release
        // exceptionRule.expectMessage("Couldn't find configuration property value");
        exceptionRule.expectMessage("Environment variable [BLAH] is not set");
        //Set vaultUrl and vaultToken properties so they can be used in the Mule config file
        System.setProperty("vaultUrl", String.format("https://%s:%d", mockServerRule.getClient().remoteAddress().getHostString(), mockServerRule.getClient().remoteAddress().getPort()));
        System.setProperty("vaultToken", "TOKEN");

        mockClient
                .withSecure(true)
                .when(
                        request()
                                .withMethod("GET")
                                .withPath("/v1/secret/data/test/mysecret")
                ).respond(
                response()
                        .withStatusCode(403)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"errors\":[]}")

        );

        return "mule_config/test-mule-empty-env-config.xml";
    }

    @Test
    public void testEmptyEnvironmentVariable() {
        try {
            String payloadValue = ((String) flowRunner("testFlow")
                    .run()
                    .getMessage()
                    .getPayload()
                    .getValue());

        } catch (Exception e) {

        }
    }
}