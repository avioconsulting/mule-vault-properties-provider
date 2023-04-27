package com.avioconsulting.mule.vault.provider.api.connection.parameters;

import org.mule.runtime.api.meta.ExpressionSupport;
import org.mule.runtime.config.api.dsl.model.ConfigurationParameters;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.Expression;
import org.mule.runtime.extension.api.annotation.dsl.xml.ParameterDsl;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;

import static com.avioconsulting.mule.vault.provider.internal.extension.VaultPropertiesProviderExtension.EXTENSION_NAMESPACE;
import static org.mule.runtime.api.component.ComponentIdentifier.builder;

public class TlsContext {

    private static final Logger logger = LoggerFactory.getLogger(TlsContext.class);

    @Parameter
    @ParameterDsl(allowReferences = false)
    @Expression(ExpressionSupport.NOT_SUPPORTED)
    @Alias("trust-store")
    @DisplayName("Trust Store Configuration")
    private TrustStoreConfig trustStoreConfig;

    @Parameter
    @ParameterDsl(allowReferences = false)
    @Expression(ExpressionSupport.NOT_SUPPORTED)
    @Alias("key-store")
    @DisplayName("Key Store Configuration")
    private KeyStoreConfig keyStoreConfig;

    public TlsContext() {
        super();
    }

    public TlsContext(ConfigurationParameters parameters) {
        List<ConfigurationParameters> tlsContextList = parameters
                .getComplexConfigurationParameter(builder()
                        .namespace(EXTENSION_NAMESPACE)
                        .name("tls-context").build());

        if (tlsContextList.size() > 0) {
            logger.info("Found TLS Context");
            ConfigurationParameters tlsParameters = tlsContextList.get(0);

            List<ConfigurationParameters> trustStoreParams = tlsParameters.getComplexConfigurationParameter(builder().namespace(EXTENSION_NAMESPACE).name("trust-store").build());
            if (trustStoreParams.size() > 0) {
                logger.info("Found Trust Store Config");
                this.trustStoreConfig = new TrustStoreConfig(trustStoreParams.get(0));
            }

            List<ConfigurationParameters> keyStoreParams = tlsParameters.getComplexConfigurationParameter(builder().namespace(EXTENSION_NAMESPACE).name("key-store").build());
            if (keyStoreParams.size() > 0) {
                logger.info("Found Key Store Config");
                this.keyStoreConfig = new KeyStoreConfig(keyStoreParams.get(0));
            }
        }
    }

    public TrustStoreConfig getTrustStoreConfig() {
        return trustStoreConfig;
    }

    public KeyStoreConfig getKeyStoreConfig() {
        return keyStoreConfig;
    }

    public boolean isTrustStoreConfigured() {
        return trustStoreConfig != null;
    }

    public boolean isKeyStoreConfigured() {
        return keyStoreConfig != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TlsContext that = (TlsContext) o;
        return Objects.equals(trustStoreConfig, that.trustStoreConfig) && Objects.equals(keyStoreConfig, that.keyStoreConfig);
    }

    @Override
    public int hashCode() {
        return Objects.hash(trustStoreConfig, keyStoreConfig);
    }
}
