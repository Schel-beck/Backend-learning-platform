package app.service;

import app.dto.gemini.GeminiResponseDTO;
import app.dto.openai.OpenAiResponseDTO;
import app.exceptions.LLMException;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class InteractiveChat {

    private final String apiKey = System.getenv("GEMINI_API_KEY");
    private final String reserveApiKey = System.getenv("OPENAI_API_KEY");

    private static final String GEMINI_MODEL = "gemini-3.5-flash-lite";
    private static final String OPENAI_MODEL = "gpt-5.6-luna";

    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final Logger log = LoggerFactory.getLogger(InteractiveChat.class);


    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public String chat(List<Map<String, Object>> contents) {
        APIReader apiReader = new APIReader();
        try {
            log.info("Trying Gemini...");
            String geminiJson = callGemini(contents);

            log.info("Gemini responded successfully");
            GeminiResponseDTO response = apiReader.convertFromJson(geminiJson, GeminiResponseDTO.class);


            return apiReader.extractTextFromGemini(response);

        } catch (LLMException e) {
            if (!e.isRetryable()) {
                throw e;
            }
            log.info("Trying OpenAI fallback...");


            try {
                String openAiJson = callReserveLLM(contents);

                OpenAiResponseDTO response = apiReader.convertFromJson(openAiJson, OpenAiResponseDTO.class);

                return apiReader.extractTextFromOpenAi(response);
            } catch (LLMException openAiException) {
                throw openAiException;
            } catch (Exception openAiException) {
                throw new LLMException(
                        "OpenAI request failed",
                        false,
                        openAiException
                );
            }
        }
    }


    private String callGemini(List<Map<String, Object>> contents) {

        if (apiKey == null || apiKey.isBlank()) {
            throw new LLMException(
                    "GEMINI_API_KEY is not configured",
                    false
            );
        }

        try {
            Map<String, Object> body = Map.of("contents", contents);
            String jsonBody = objectMapper.writeValueAsString(body);

            String endpoint =
                    "https://generativelanguage.googleapis.com/v1beta/models/"
                            + GEMINI_MODEL
                            + ":generateContent";

            log.info("Sending request to Gemini model: {}", GEMINI_MODEL);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .timeout(Duration.ofSeconds(30))
                    .header("Content-Type", "application/json")
                    .header("x-goog-api-key", apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = client.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            int status = response.statusCode();

            log.info("Gemini HTTP status: {}", status);

            if (status < 200 || status >= 300) {
                log.error("Gemini error response: {}", response.body());

                throw new LLMException(
                        "Gemini returned HTTP " + status + ": "
                                + response.body(),
                        isRetryableStatus(status)
                );
            }

            log.info("Gemini request succeeded");
            return response.body();

        } catch (HttpTimeoutException e) {
            log.error("Gemini timed out", e);
            throw new LLMException("Gemini request timed out", true, e);

        } catch (JsonProcessingException e) {
            log.error("Could not serialize Gemini request", e);
            throw new LLMException(
                    "Could not create Gemini JSON request", false, e
            );

        } catch (IOException e) {
            log.error("Gemini network error", e);
            throw new LLMException("Gemini network error", true, e);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Gemini request interrupted", e);
            throw new LLMException(
                    "Gemini request was interrupted", true, e
            );
        }
    }

    public String callReserveLLM(
            List<Map<String, Object>> contents
    ) throws Exception {

        if (reserveApiKey == null || reserveApiKey.isBlank()) {
            throw new LLMException(
                    "OPENAI_API_KEY is not configured",
                    false
            );
        }
        List<Map<String, Object>> input =
                convertGeminiContentsToOpenAIInput(contents);

        Map<String, Object> body = Map.of(
                "model", OPENAI_MODEL,
                "input", input
        );

        String jsonBody =
                objectMapper.writeValueAsString(body);

        String endpoint =
                "https://api.openai.com/v1/responses";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .timeout(Duration.ofSeconds(30))
                .header("Content-Type", "application/json")
                .header(
                        "Authorization",
                        "Bearer " + reserveApiKey
                )
                .POST(
                        HttpRequest.BodyPublishers
                                .ofString(jsonBody)
                )
                .build();

        try {

            HttpResponse<String> response =
                    client.send(
                            request,
                            HttpResponse.BodyHandlers.ofString()
                    );

            int status = response.statusCode();

            if (status != 200) {

                throw new LLMException(
                        "OpenAI returned HTTP "
                                + status
                                + ": "
                                + response.body(),
                        isRetryableStatus(status)
                );
            }

            return response.body();

        } catch (HttpTimeoutException e) {

            throw new LLMException(
                    "OpenAI request timed out",
                    true,
                    e
            );

        } catch (IOException e) {

            throw new LLMException(
                    "OpenAI network error: "
                            + e.getMessage(),
                    true,
                    e
            );

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();

            throw new LLMException(
                    "OpenAI request was interrupted",
                    true,
                    e
            );
        }
    }

    public List<Map<String, Object>>
    convertGeminiContentsToOpenAIInput(
            List<Map<String, Object>> contents
    ) {

        List<Map<String, Object>> input =
                new ArrayList<>();

        for (Map<String, Object> content : contents) {

            String role =
                    (String) content.get("role");

            // Gemini kalder assistant "model"
            // OpenAI kalder den "assistant"
            if ("model".equals(role)) {
                role = "assistant";
            }

            List<Map<String, Object>> parts =
                    (List<Map<String, Object>>)
                            content.get("parts");

            StringBuilder text =
                    new StringBuilder();

            for (Map<String, Object> part : parts) {

                Object textObject =
                        part.get("text");

                if (textObject != null) {

                    if (text.length() > 0) {
                        text.append("\n");
                    }

                    text.append(textObject);
                }
            }

            input.add(
                    Map.of(
                            "role", role,
                            "content", text.toString()
                    )
            );
        }

        return input;
    }


    // ---------------------------------------------------------
    // JSON -> GeminiResponse
    // ---------------------------------------------------------


    public static Map<String, Object> turn(
            String role,
            String text
    ) {

        return Map.of(
                "role", role,
                "parts", List.of(
                        Map.of("text", text)
                )
        );
    }

    private boolean isRetryableStatus(int status) {

        return status == 408
                || status == 429
                || status == 500
                || status == 502
                || status == 503
                || status == 504;
    }


}