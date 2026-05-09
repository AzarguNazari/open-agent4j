package org.openagent4j.provider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class ProviderRegistryTest {

    @Test
    void defaultsExposeBuiltInProviders() {
        ProviderDescriptor openai =
                ProviderRegistry.defaults().find("OPENAI").orElseThrow(() -> new AssertionError("missing openai"));

        assertEquals("openai", openai.id());
        assertEquals("https://api.openai.com/v1", openai.defaultBaseUrl());
        assertTrue(openai.apiKeyEnvFallbacks().contains("OPENAI_API_KEY"));
    }

    @Test
    void resolveSupportsAdHocProvidersWithoutBuiltInDefaults() {
        ProviderDescriptor adHoc = ProviderRegistry.defaults().resolve("Acme-AI");

        assertEquals("acme-ai", adHoc.id());
        assertNull(adHoc.defaultBaseUrl());
        assertTrue(adHoc.apiKeyEnvFallbacks().isEmpty());
    }

    @Test
    void registryBuilderAllowsCustomProviderRegistration() {
        ProviderRegistry registry = ProviderRegistry.defaults()
                .toBuilder()
                .register(new ProviderDescriptor("acme", "https://api.acme.test/v1", List.of("ACME_API_KEY"), List.of()))
                .build();

        ProviderDescriptor acme = registry.find("acme").orElseThrow(() -> new AssertionError("missing acme"));
        assertEquals("https://api.acme.test/v1", acme.defaultBaseUrl());
        assertTrue(acme.apiKeyEnvFallbacks().contains("ACME_API_KEY"));
    }
}
