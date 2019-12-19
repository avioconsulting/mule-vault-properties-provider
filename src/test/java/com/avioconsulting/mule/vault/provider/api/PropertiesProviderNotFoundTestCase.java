package com.avioconsulting.mule.vault.provider.api;

import com.bettercloud.vault.VaultException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockserver.client.MockServerClient;
import org.mockserver.junit.MockServerRule;
import org.mule.functional.junit4.MuleArtifactFunctionalTestCase;

import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

public class PropertiesProviderNotFoundTestCase extends MuleArtifactFunctionalTestCase {
    @Rule
    public MockServerRule mockServerRule = new MockServerRule(this);
    private MockServerClient mockClient;

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Override
    protected String getConfigFile() {
        exceptionRule.expectMessage("Couldn't find configuration property value");
        //Set vaultUrl and vaultToken properties so they can be used in the Mule config file
        System.setProperty("vaultUrl", String.format("https://%s:%d", mockServerRule.getClient().remoteAddress().getHostString(), mockServerRule.getClient().remoteAddress().getPort()));
        System.setProperty("vaultToken", "TOKEN");
        System.setProperty("ENV", "test");

        mockClient
                .withSecure(true)
                .when(
                        request()
                                .withMethod("GET")
                                .withPath("/v1/secret/data/test/mysecret")
                ).respond(
                response()
                        .withStatusCode(404)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"errors\":[]}")

        );

        return "mule_config/test-mule-secret-not-found-config.xml";
    }

    @Test
    public void testPropertyNotFound() {
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