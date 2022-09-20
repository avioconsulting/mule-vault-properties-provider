package com.avioconsulting.mule.vault.provider.api;

import com.bettercloud.vault.Vault;
import com.bettercloud.vault.VaultException;
import com.bettercloud.vault.api.Logical;
import com.bettercloud.vault.response.LogicalResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mule.runtime.config.api.dsl.model.properties.ConfigurationProperty;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;


@ExtendWith(MockitoExtension.class)
@ExtendWith(SystemStubsExtension.class)
public class VaultConfigurationPropertiesProviderTestCase {

	public static final String PROPERTIES_LOCATION_PATH = "files/local.properties";
	private VaultConfigurationPropertiesProvider propertiesProvider;
	private VaultConfigurationPropertiesProvider noLocalPropertiesProvider;
	private @Mock Vault vault;
	private @Mock Logical logical;
	private @Mock LogicalResponse logicalResponse;

	@SystemStub
	private EnvironmentVariables environmentVariables;


	@BeforeEach
	private void init() throws VaultException {

		if(propertiesProvider == null) {
			propertiesProvider = new VaultConfigurationPropertiesProvider(vault, true, PROPERTIES_LOCATION_PATH);
		}

		if(noLocalPropertiesProvider == null) {
			noLocalPropertiesProvider = new VaultConfigurationPropertiesProvider(vault, false, PROPERTIES_LOCATION_PATH);
		}

		Mockito.lenient().when(vault.logical()).thenReturn(logical);
		Mockito.lenient().when(logical.read(anyString())).thenReturn(logicalResponse);
		Mockito.lenient().when(logicalResponse.getData()).thenReturn(getMockMapAsVaultResponse());

		environmentVariables.set("ENV", "test");

	}

	@Test
	public void test_get_not_existing_property_local_mode_true() {
		Optional<ConfigurationProperty> prop =  propertiesProvider.getConfigurationProperty("vault:secret/test/mynonexistingprop.prop");
		assertFalse(prop.isPresent());
	}

	@Test
	public void test_get_not_existing_property_key_local_mode_true() {
		Optional<ConfigurationProperty> prop =  propertiesProvider.getConfigurationProperty("vault::secret/test/mysecret.notInSecret");
		assertFalse(prop.isPresent());
	}

	@Test
	public void test_get_not_property_key_specified_local_mode_true() {
		Optional<ConfigurationProperty> prop =  propertiesProvider.getConfigurationProperty("vault::secret/test/mysecret");
		assertFalse(prop.isPresent());
	}


	@Test
	public void test_get_existing_property_key_local_mode_true() {
		Optional<ConfigurationProperty> prop =  propertiesProvider.getConfigurationProperty("vault::secret/test/mysecret.att1");
		assertTrue(prop.isPresent());
		assertEquals("I-came-from-file",prop.get().getRawValue());
	}

	@Test
	public void test_get_not_existing_property_key_local_mode_false() {
		Optional<ConfigurationProperty> prop =  noLocalPropertiesProvider.getConfigurationProperty("vault::secret/test/mysecret.notInVault");
		assertFalse(prop.isPresent());
	}

	@Test
	public void test_get_existing_property_key_local_mode_false() {
		Optional<ConfigurationProperty> prop =  noLocalPropertiesProvider.getConfigurationProperty("vault::secret/test/mysecret.att3");
		assertTrue(prop.isPresent());
		assertEquals("I-came-from-vault!",prop.get().getRawValue());
	}

	@Test
	public void test_get_existing_env_property_key_local_mode_false() {
		Optional<ConfigurationProperty> prop =  noLocalPropertiesProvider.getConfigurationProperty("vault::secret/$[ENV]/mysecret.att3");
		assertTrue(prop.isPresent());
		assertEquals("I-came-from-vault!",prop.get().getRawValue());
	}

	private Map<String,String> getMockMapAsVaultResponse(){
		return Collections.singletonMap("att3", "I-came-from-vault!");
	}
}