package com.avioconsulting.mule.vault.provider;

import org.junit.Rule;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import org.mockserver.client.MockServerClient;
import org.mockserver.junit.MockServerRule;
import org.mule.functional.junit4.MuleArtifactFunctionalTestCase;

public class VaultTokenConnectionProviderTestCase extends MuleArtifactFunctionalTestCase{


    @Rule
    public MockServerRule mockServerRule = new MockServerRule(this);
    private MockServerClient mockClient;

    @Override
    protected String getConfigFile() {
        // Set vaultUrl and vaultToken properties so they can be used in the Mule config file
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
                        .withStatusCode(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"request_id\": \"69411f4b-fb02-171a-f66c-485f106e7f5c\",\"lease_id\": \"\",\"renewable\": false,\"lease_duration\": 0,\"data\": {\"data\": {\"att1\": \"test_value1\",\"att2\": \"test_value2\"},\"metadata\": {\"created_time\": \"2019-04-24T23:03:18.63231658Z\",\"deletion_time\": \"\",\"destroyed\": false,\"version\": 1}},\"wrap_info\": null,\"warnings\": null,\"auth\": null}")

        );

        return "mule_config/test-mule-token-config.xml";
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
}
