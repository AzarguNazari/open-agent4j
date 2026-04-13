package org.openagent4j.execution;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.RecordComponent;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.concurrent.TimeUnit;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.openagent4j.config.ProviderSettings;
import org.openagent4j.provider.ProviderDescriptor;
import org.openagent4j.provider.ProviderRegistry;

/**
 * Calls an OpenAI-compatible chat completions API using {@link LlmRequest#providerSettings()}.
 *
 * <p>Built-in providers from {@link ProviderRegistry#defaults()} supply a default base URL when none is configured.
 * For other provider ids, set {@code openagent4j.<provider>.base-url} or {@code OPENAGENT4J_<PROVIDER>_BASE_URL}.
 */
public final class OpenAiCompatibleLlmExecutor implements LlmExecutor {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json; charset=utf-8");
    private static final int TIMEOUT_MINUTES = 2;

    private final OkHttpClient httpClient;

    public OpenAiCompatibleLlmExecutor() {
        this(new OkHttpClient.Builder()
                .connectTimeout(TIMEOUT_MINUTES, TimeUnit.MINUTES)
                .readTimeout(TIMEOUT_MINUTES, TimeUnit.MINUTES)
                .writeTimeout(TIMEOUT_MINUTES, TimeUnit.MINUTES)
                .build());
    }

    public OpenAiCompatibleLlmExecutor(OkHttpClient httpClient) {
        this.httpClient = Objects.requireNonNull(httpClient, "httpClient");
    }

    @Override
    public String complete(LlmRequest request) {
        Objects.requireNonNull(request.model(), "request.model");
        ProviderSettings settings = request.providerSettings();
        String apiKey = settings.apiKey();
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                    "Missing API key for provider '"
                            + settings.providerId()
                            + "'. Configure openagent4j."
                            + settings.providerId()
                            + ".api-key, OPENAGENT4J_"
                            + settings.providerId().toUpperCase().replace('-', '_')
                            + "_API_KEY, or a provider-specific env var (see LlmProvider).");
        }
        String base = effectiveBaseUrl(settings);
        String url = trimTrailingSlash(base) + "/chat/completions";
        String modelName = request.model().modelName();

        ObjectNode body = MAPPER.createObjectNode();
        body.put("model", modelName);
        ArrayNode messages = body.putArray("messages");
        String systemMessage = request.systemMessage();
        if (request.expectsStructuredResponse()) {
            ObjectNode responseFormat = body.putObject("response_format");
            responseFormat.put("type", "json_object");
            systemMessage = systemMessage + structuredOutputInstruction(request.responseType());
        }
        ObjectNode system = messages.addObject();
        system.put("role", "system");
        system.put("content", systemMessage);
        ObjectNode user = messages.addObject();
        user.put("role", "user");
        user.put("content", request.taskPrompt());

        String json;
        try {
            json = MAPPER.writeValueAsString(body);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to serialize request", e);
        }

        Request httpRequest = new Request.Builder()
                .url(url)
                .header("Authorization", "Bearer " + apiKey)
                .post(RequestBody.create(json.getBytes(StandardCharsets.UTF_8), JSON_MEDIA_TYPE))
                .build();

        try (Response response = httpClient.newCall(httpRequest).execute()) {
            ResponseBody responseBody = response.body();
            String responseText = responseBody != null ? responseBody.string() : "";
            if (!response.isSuccessful()) {
                throw new IllegalStateException("Chat API HTTP " + response.code() + ": " + responseText);
            }
            JsonNode root = MAPPER.readTree(responseText);
            JsonNode err = root.get("error");
            if (err != null && !err.isNull()) {
                String msg = err.hasNonNull("message") ? err.get("message").asText() : err.toString();
                throw new IllegalStateException("Chat API error: " + msg);
            }
            JsonNode choices = root.get("choices");
            if (choices == null || !choices.isArray() || choices.isEmpty()) {
                throw new IllegalStateException("Unexpected response (no choices): " + responseText);
            }
            JsonNode content = choices.get(0).path("message").path("content");
            if (content.isMissingNode() || content.isNull()) {
                throw new IllegalStateException("Unexpected response (no message content): " + responseText);
            }
            return content.asText();
        } catch (IOException e) {
            throw new IllegalStateException("Chat request failed: " + e.getMessage(), e);
        }
    }

    private static String effectiveBaseUrl(ProviderSettings settings) {
        String configured = settings.baseUrl();
        if (configured != null && !configured.isBlank()) {
            return configured.trim();
        }
        return ProviderRegistry.defaults().find(settings.providerId())
                .map(ProviderDescriptor::defaultBaseUrl)
                .orElseThrow(() -> new IllegalStateException(
                        "No base URL for provider '"
                                + settings.providerId()
                                + "'. Add openagent4j."
                                + settings.providerId()
                                + ".base-url, OPENAGENT4J_"
                                + settings.providerId().toUpperCase().replace('-', '_')
                                + "_BASE_URL, or register a default on ProviderRegistry."));
    }

    private static String trimTrailingSlash(String base) {
        if (base.endsWith("/")) {
            return base.substring(0, base.length() - 1);
        }
        return base;
    }

    private static String structuredOutputInstruction(Class<?> responseType) {
        StringJoiner fields = new StringJoiner(", ");
        if (responseType.isRecord()) {
            for (RecordComponent component : responseType.getRecordComponents()) {
                fields.add("\"" + component.getName() + "\": \"" + typeLabel(component.getType()) + "\"");
            }
        } else {
            for (Field field : responseType.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers()) || field.isSynthetic()) {
                    continue;
                }
                fields.add("\"" + field.getName() + "\": \"" + typeLabel(field.getType()) + "\"");
            }
        }
        String shape = fields.length() == 0 ? "{}" : "{" + fields + "}";
        return "\nReturn ONLY valid JSON that can be deserialized to "
                + responseType.getName()
                + ". Expected shape: "
                + shape
                + ".";
    }

    private static String typeLabel(Class<?> type) {
        if (type.isArray()) {
            return "array<" + typeLabel(type.getComponentType()) + ">";
        }
        if (type == String.class || type == char.class || type == Character.class) {
            return "string";
        }
        if (type == boolean.class || type == Boolean.class) {
            return "boolean";
        }
        if (type.isPrimitive() || Number.class.isAssignableFrom(type)) {
            return "number";
        }
        return "object";
    }
}
