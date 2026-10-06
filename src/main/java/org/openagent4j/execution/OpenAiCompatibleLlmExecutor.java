package org.openagent4j.execution;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
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

    private static final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json; charset=utf-8");
    private static final String CHAT_COMPLETIONS_PATH = "/chat/completions";
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
        ProviderSettings settings = Objects.requireNonNull(request.providerSettings(), "request.providerSettings");
        String apiKey = requireApiKey(settings);
        String url = trimTrailingSlash(effectiveBaseUrl(settings)) + CHAT_COMPLETIONS_PATH;
        String json = ChatCompletionRequestWriter.write(request);

        Request httpRequest = new Request.Builder()
                .url(url)
                .header("Authorization", "Bearer " + apiKey)
                .post(RequestBody.create(json.getBytes(StandardCharsets.UTF_8), JSON_MEDIA_TYPE))
                .build();

        try (Response response = httpClient.newCall(httpRequest).execute()) {
            String responseText = readBody(response);
            if (!response.isSuccessful()) {
                throw new IllegalStateException("Chat API HTTP " + response.code() + ": " + responseText);
            }
            return ChatCompletionResponseReader.readContent(responseText);
        } catch (IOException exception) {
            throw new IllegalStateException("Chat request failed: " + exception.getMessage(), exception);
        }
    }

    private static String readBody(Response response) throws IOException {
        ResponseBody responseBody = response.body();
        if (responseBody == null) {
            return "";
        }
        return responseBody.string();
    }

    private static String requireApiKey(ProviderSettings settings) {
        String apiKey = settings.apiKey();
        if (apiKey != null && !apiKey.isBlank()) {
            return apiKey;
        }
        throw new IllegalStateException(
                "Missing API key for provider '"
                        + settings.providerId()
                        + "'. Configure openagent4j."
                        + settings.providerId()
                        + ".api-key, OPENAGENT4J_"
                        + ProviderSettings.environmentToken(settings.providerId())
                        + "_API_KEY, or a provider-specific env var (see LlmProvider).");
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
                                + ProviderSettings.environmentToken(settings.providerId())
                                + "_BASE_URL, or register a default on ProviderRegistry."));
    }

    private static String trimTrailingSlash(String base) {
        if (base.endsWith("/")) {
            return base.substring(0, base.length() - 1);
        }
        return base;
    }
}
