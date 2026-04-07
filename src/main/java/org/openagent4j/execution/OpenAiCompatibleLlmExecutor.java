package org.openagent4j.execution;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Objects;
import org.openagent4j.config.ProviderSettings;
import org.openagent4j.provider.LlmProvider;

/**
 * Calls an OpenAI-compatible chat completions API using {@link LlmRequest#providerSettings()}.
 *
 * <p>Built-in {@link LlmProvider} entries supply a default base URL when none is configured. For other provider ids,
 * set {@code openagent4j.<provider>.base-url} or {@code OPENAGENT4J_<PROVIDER>_BASE_URL}.
 */
public final class OpenAiCompatibleLlmExecutor implements LlmExecutor {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Duration TIMEOUT = Duration.ofMinutes(2);

    private final HttpClient httpClient;

    public OpenAiCompatibleLlmExecutor() {
        this(HttpClient.newBuilder().connectTimeout(TIMEOUT).build());
    }

    public OpenAiCompatibleLlmExecutor(HttpClient httpClient) {
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
        ObjectNode system = messages.addObject();
        system.put("role", "system");
        system.put("content", request.systemMessage());
        ObjectNode user = messages.addObject();
        user.put("role", "user");
        user.put("content", request.taskPrompt());

        String json;
        try {
            json = MAPPER.writeValueAsString(body);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to serialize request", e);
        }

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(TIMEOUT)
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                .build();

        try {
            HttpResponse<String> response =
                    httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException(
                        "Chat API HTTP " + response.statusCode() + ": " + response.body());
            }
            JsonNode root = MAPPER.readTree(response.body());
            JsonNode err = root.get("error");
            if (err != null && !err.isNull()) {
                String msg = err.hasNonNull("message") ? err.get("message").asText() : err.toString();
                throw new IllegalStateException("Chat API error: " + msg);
            }
            JsonNode choices = root.get("choices");
            if (choices == null || !choices.isArray() || choices.isEmpty()) {
                throw new IllegalStateException("Unexpected response (no choices): " + response.body());
            }
            JsonNode content = choices.get(0).path("message").path("content");
            if (content.isMissingNode() || content.isNull()) {
                throw new IllegalStateException("Unexpected response (no message content): " + response.body());
            }
            return content.asText();
        } catch (IOException e) {
            throw new IllegalStateException("Chat request failed: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Chat request interrupted", e);
        }
    }

    private static String effectiveBaseUrl(ProviderSettings settings) {
        String configured = settings.baseUrl();
        if (configured != null && !configured.isBlank()) {
            return configured.trim();
        }
        return LlmProvider.byId(settings.providerId())
                .map(LlmProvider::defaultBaseUrl)
                .orElseThrow(() -> new IllegalStateException(
                        "No base URL for provider '"
                                + settings.providerId()
                                + "'. Add openagent4j."
                                + settings.providerId()
                                + ".base-url, OPENAGENT4J_"
                                + settings.providerId().toUpperCase().replace('-', '_')
                                + "_BASE_URL, or register a default on LlmProvider."));
    }

    private static String trimTrailingSlash(String base) {
        if (base.endsWith("/")) {
            return base.substring(0, base.length() - 1);
        }
        return base;
    }
}
