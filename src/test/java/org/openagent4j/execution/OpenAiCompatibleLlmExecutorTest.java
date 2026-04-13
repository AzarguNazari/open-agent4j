package org.openagent4j.execution;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import org.openagent4j.config.ProviderSettings;
import org.openagent4j.model.Model;

class OpenAiCompatibleLlmExecutorTest {

    record Answer(String answer) {}

    @Test
    void throwsWhenBaseUrlMissingForUnknownProvider() {
        LlmRequest request = new LlmRequest(
                "n",
                "a",
                null,
                "task",
                Model.of("unknown-vendor", "some-model"),
                null,
                List.of(),
                List.of(),
                null,
                null,
                null,
                null,
                null,
                null,
                new ProviderSettings("unknown-vendor", "sk-test", null));

        assertThrows(IllegalStateException.class, () -> new OpenAiCompatibleLlmExecutor().complete(request));
    }

    @Test
    void addsStructuredResponseHintsWhenTypedResponseIsRequested() throws IOException {
        AtomicReference<String> capturedBody = new AtomicReference<>();
        OkHttpClient client = clientWithCapture(capturedBody);

        LlmRequest request = new LlmRequest(
                "n",
                "a",
                "You are helpful.",
                "task",
                Model.of("openai", "gpt-4o"),
                null,
                List.of(),
                List.of(),
                null,
                null,
                null,
                null,
                null,
                Answer.class,
                new ProviderSettings("openai", "sk-test", "https://example.org/v1"));

        String response = new OpenAiCompatibleLlmExecutor(client).complete(request);

        assertEquals("{\"answer\":\"4\"}", response);
        String sentJson = capturedBody.get();
        assertTrue(sentJson.contains("\"response_format\":{\"type\":\"json_object\"}"));
        assertTrue(sentJson.contains(Answer.class.getName()));
        assertTrue(sentJson.contains("Return ONLY valid JSON"));
    }

    @Test
    void doesNotAddStructuredResponseHintsWhenResponseTypeIsUnset() throws IOException {
        AtomicReference<String> capturedBody = new AtomicReference<>();
        OkHttpClient client = clientWithCapture(capturedBody);

        LlmRequest request = new LlmRequest(
                "n",
                "a",
                "You are helpful.",
                "task",
                Model.of("openai", "gpt-4o"),
                null,
                List.of(),
                List.of(),
                null,
                null,
                null,
                null,
                null,
                null,
                new ProviderSettings("openai", "sk-test", "https://example.org/v1"));

        new OpenAiCompatibleLlmExecutor(client).complete(request);

        String sentJson = capturedBody.get();
        assertTrue(!sentJson.contains("\"response_format\""));
    }

    private static OkHttpClient clientWithCapture(AtomicReference<String> capturedBody) {
        Interceptor capture = chain -> {
            Request request = chain.request();
            Buffer buffer = new Buffer();
            if (request.body() != null) {
                request.body().writeTo(buffer);
            }
            capturedBody.set(buffer.readUtf8());

            String okResponse = """
                    {"choices":[{"message":{"content":"{\\"answer\\":\\"4\\"}"}}]}
                    """;
            return new Response.Builder()
                    .request(request)
                    .protocol(Protocol.HTTP_1_1)
                    .code(200)
                    .message("OK")
                    .body(ResponseBody.create(okResponse, MediaType.parse("application/json")))
                    .build();
        };
        return new OkHttpClient.Builder().addInterceptor(capture).build();
    }
}
