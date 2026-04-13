package org.openagent4j.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.openagent4j.provider.KnownModel;

class ModelRegistryTest {

    @Test
    void defaultsIncludeKnownModelAliases() {
        Model model = ModelRegistry.defaults()
                .find(KnownModel.GPT_35_TURBO.alias())
                .orElseThrow(() -> new AssertionError("missing known alias"));

        assertEquals("openai", model.provider());
        assertEquals("gpt-3.5-turbo", model.modelName());
    }

    @Test
    void builderAllowsExtendingCatalogWithoutChangingEnums() {
        ModelRegistry registry = ModelRegistry.defaults()
                .toBuilder()
                .register("acme.fast", Model.of("acme", "fast-1"))
                .build();

        Model custom = registry.require("ACME.FAST");
        assertEquals("acme", custom.provider());
        assertEquals("fast-1", custom.modelName());
    }

    @Test
    void providerFacadesExposeGenericFactoryMethods() {
        assertEquals("openai", OpenApi.of("gpt-4o-mini").provider());
        assertEquals("gpt-4o-mini", OpenApi.of("gpt-4o-mini").modelName());

        assertEquals("deepseek", DeepSeek.of("deepseek-reasoner").provider());
        assertEquals("deepseek-reasoner", DeepSeek.of("deepseek-reasoner").modelName());
        assertTrue(ModelRegistry.defaults().aliases().contains(KnownModel.DEEPSEEK_CHAT.alias()));
    }
}
