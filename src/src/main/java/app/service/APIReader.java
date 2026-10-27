package app.service;
import app.controllers.QuestionController;
import app.dto.gemini.CandidateDTO;
import app.dto.gemini.GeminiResponseDTO;
import app.dto.openai.OpenAiResponseDTO;
import app.dto.openai.OutputDTO;
import app.exceptions.ApiException;
import app.exceptions.LLMException;
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
import java.util.List;
import java.util.Map;

public class APIReader {
    String apiKey = System.getenv("GEMINI_API_KEY");
    private final String reserveApiKey = System.getenv("OPENAI_API_KEY");
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final Logger log = LoggerFactory.getLogger(APIReader.class);




    public String readAPI(String prompt) {

        try {

            return geminiReadApi(prompt);

        } catch (LLMException geminiException) {

            log.warn("Gemini failed: " + geminiException.getMessage());


            if (!geminiException.isRetryable()) {
                throw geminiException;
            }
            log.info("Trying OpenAI reserve llm...");
        }
        return "retryable";
    }
    public String geminiReadApi(String prompt) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                    "GEMINI_API_KEY is not configured"
            );
        }
        try {

            HttpClient client = HttpClient.newHttpClient();

            Map<String, Object> body = Map.of(
                    "contents", List.of(
                            Map.of(
                                    "parts", List.of(
                                            Map.of(
                                                    "text",
                                                    prompt
                                            )
                                    )
                            )
                    )
            );

            String jsonBody = objectMapper.writeValueAsString(body);

            String model = "gemini-3.5-flash-lite";

            String endpoint =
                    "https://generativelanguage.googleapis.com/v1beta/models/"
                            + model
                            + ":generateContent";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .header("Content-Type", "application/json")
                    .header("x-goog-api-key", apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            // Send the request and get the response
            HttpResponse<String> response =
                    client.send(
                            request,
                            HttpResponse.BodyHandlers.ofString()
                    );

            if (response.statusCode() != 200) {
                throw new LLMException(
                        "Gemini returned HTTP " + response.statusCode()
                                + ": " + response.body(),
                        isRetryableStatus(response.statusCode())
                );
            }
            return response.body();
        } catch (LLMException e) {
            throw e;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();

            throw new LLMException(
                    "Gemini request was interrupted",
                    true,
                    e
            );
        } catch (java.net.http.HttpTimeoutException e) {
            throw new LLMException(
                    "Gemini request timed out",
                    true,
                    e
            );
        } catch (IOException e) {
            throw new LLMException(
                    "Gemini network error: " + e.getMessage(),
                    true,
                    e
            );
        }
    }
    public String callReserveLLM(String prompt) throws Exception {

        if (reserveApiKey == null || reserveApiKey.isBlank()) {
            throw new LLMException(
                    "OPENAI_API_KEY is not configured",
                    false
            );
        }

        String OPENAI_MODEL = "gpt-5.6-luna";
        Map<String, Object> body = Map.of(
                "model", OPENAI_MODEL,
                "input", prompt
        );

        String jsonBody = objectMapper.writeValueAsString(body);

        String endpoint =
                "https://api.openai.com/v1/responses";

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(60))
                .build();

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

            HttpResponse<String> response = client.send(
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




    public <T> T convertFromJson(String json, Class<T> tClass) {
        try {
            return objectMapper.readValue(json, tClass);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error converting JSON", e);
        }
    }







    public boolean isRetryableStatus(int status) {

        return status == 408
                || status == 429
                || status == 500
                || status == 502
                || status == 503
                || status == 504;
    }



    public String extractTextFromGemini(GeminiResponseDTO response) {

        if (response == null) {
            throw new ApiException(
                    500,
                    "Gemini response is null"
            );
        }

        if (response.getCandidates() == null ||
                response.getCandidates().isEmpty()) {

            throw new ApiException(
                    500,
                    "Gemini response contains no candidates"
            );
        }

        CandidateDTO candidate =
                response.getCandidates().get(0);

        if (candidate.getContent() == null) {
            throw new ApiException(
                    500,
                    "Gemini candidate contains no content"
            );
        }

        if (candidate.getContent().getParts() == null ||
                candidate.getContent().getParts().isEmpty()) {

            throw new ApiException(
                    500,
                    "Gemini content contains no parts"
            );
        }

        String text = candidate.getContent()
                .getParts()
                .get(0)
                .getText();

        if (text == null || text.isBlank()) {
            throw new ApiException(
                    500,
                    "Gemini response contains no text"
            );
        }

        return text;
    }
    public String extractTextFromOpenAi(OpenAiResponseDTO response) {

        if (response == null) {
            throw new ApiException(
                    500,
                    "Gemini response is null"
            );
        }

        if (response.getOutput() == null ||
                response.getOutput().isEmpty()) {

            throw new ApiException(
                    500,
                    "Openai response contains no output"
            );
        }

        OutputDTO outputDTO =
                response.getOutput().get(0);

        if (outputDTO.getContent() == null) {
            throw new ApiException(
                    500,
                    "Openai output contains no content"
            );
        }

        if (outputDTO.getContent() == null ||
                outputDTO.getContent().isEmpty()) {

            throw new ApiException(
                    500,
                    "Openai content contains no text"
            );
        }

        String text = outputDTO.getContent()
                .get(0)
                .getText();

        if (text == null || text.isBlank()) {
            throw new ApiException(
                    500,
                    "Openai response contains no text"
            );
        }

        return text;
    }
}