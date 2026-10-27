package app.service;

import app.controllers.QuestionController;
import app.dto.*;
import app.dto.gemini.GeminiResponseDTO;
import app.dto.openai.OpenAiResponseDTO;
import app.exceptions.ApiException;
import app.exceptions.LLMException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Fetcher {
    private static final Logger log = LoggerFactory.getLogger(Fetcher.class);

    public List<SubjectsWithQuestionsDTO> fetchSubjectsAndQuestions() throws Exception {

        APIReader apiReader = new APIReader();

        String promptForSubjects = "Du er en underviser i Java.\n" +
                "\n" +
                "Lav 14 emner som en datamatiker har på 1.semester.\n" +
                "\n" +
                "Niveau: begynder.\n" +
                "\n" +
                "Emnerne er til en person, der er begynder i Java.\n" +
                "\n" +
                "Krav:\n" +
                "- Emner skal være på dansk.\n" +
                "- Emner skal være forskellige.\n" +
                "- Emner skal være relevante for uddannelsen.\n" +
                "- Undgå meget avancerede emner.\n" +
                "- Returnér kun JSON.\n" +
                "\n" +
                "Format:\n" +
                "{\n" +
                "  \"subjects\": [\n" +
                "    {\n" +
                "      \"name\": \"subject name\",\n" +
                "      \"numberOfQuestions\": 10\n" +
                "    }\n" +
                "  ]\n" +
                "}";
        //String json = apiReader.readAPI(promptForSubjects);
        String json;
        String llmUsedForSubjects;
        SubjectListDTO subjectListDTO = null;

        try {
            json = apiReader.geminiReadApi(promptForSubjects);
            llmUsedForSubjects = "Gemini";

        } catch (LLMException e) {
            if (!e.isRetryable()) {
                throw e;
            }

            json = apiReader.callReserveLLM(promptForSubjects);
            llmUsedForSubjects = "OpenAI";

        }
        if ("Gemini".equals(llmUsedForSubjects)) {
            GeminiResponseDTO response = apiReader.convertFromJson(json, GeminiResponseDTO.class);

            String text = apiReader.extractTextFromGemini(response);

            subjectListDTO = apiReader.convertFromJson(text, SubjectListDTO.class);

        } else if ("OpenAI".equals(llmUsedForSubjects)) {
            OpenAiResponseDTO openAiResponseDTO = apiReader.convertFromJson(json, OpenAiResponseDTO.class);

            String text = apiReader.extractTextFromOpenAi(openAiResponseDTO);

            subjectListDTO = apiReader.convertFromJson(text, SubjectListDTO.class);
        }
        ExecutorService executorService = Executors.newFixedThreadPool(3);

        List<Future<SubjectsWithQuestionsDTO>> futures = new ArrayList<>();

        for (SubjectDTO subject : subjectListDTO.getSubjects()) {
            String promptForQuestions = "Du er en underviser i Java.\n" +
                    "\n" +
                    "Emnet skal være: " + subject.getName() + "\n" +
                    "\n" +
                    "Lav præcis " +
                    subject.getNumberOfQuestions() +
                    " spørgsmål om emnet.\n" +
                    "\n" +
                    "Spørgsmålene skal være relevante for en datamatiker på 1. semester.\n" +
                    "Niveau: begynder.\n" +
                    "\n" +
                    "Krav:\n" +
                    "- Spørgsmålene skal være på dansk.\n" +
                    "- Spørgsmålene skal være forskellige.\n" +
                    "- Spørgsmålene skal være relevante for emnet.\n" +
                    "- Undgå meget avancerede spørgsmål.\n" +
                    "- Returnér kun JSON.\n" +
                    "\n" +
                    "Format:\n" +
                    "{\n" +
                    "  \"questions\": [\n" +
                    "    {\n" +
                    "      \"id\": \"1,\"\n" +
                    "      \"name\": \"question name\"\n" +
                    "    }\n" +
                    "  ]\n" +
                    "}";


            Future<SubjectsWithQuestionsDTO> future =
                    executorService.submit(() -> {
                        String jsonForQuestion;
                        String llmUsed;
                        QuestionListDTO questionListDTO = null;

                        try {
                            jsonForQuestion = apiReader.geminiReadApi(promptForQuestions);
                            llmUsed = "Gemini";

                        } catch (LLMException e) {
                            if (!e.isRetryable()) {
                                throw e;
                            }

                            jsonForQuestion = apiReader.callReserveLLM(promptForQuestions);
                            llmUsed = "OpenAI";

                        }

                        if ("Gemini".equals(llmUsed)) {
                            GeminiResponseDTO response = apiReader.convertFromJson(jsonForQuestion, GeminiResponseDTO.class);

                            String text = apiReader.extractTextFromGemini(response);

                            questionListDTO = apiReader.convertFromJson(text, QuestionListDTO.class);

                        } else if ("OpenAI".equals(llmUsed)) {
                            OpenAiResponseDTO openAiResponseDTO = apiReader.convertFromJson(jsonForQuestion, OpenAiResponseDTO.class);

                            String text = apiReader.extractTextFromOpenAi(openAiResponseDTO);

                            questionListDTO = apiReader.convertFromJson(text, QuestionListDTO.class);
                        }

                        return new SubjectsWithQuestionsDTO(
                                subject,
                                questionListDTO.getQuestions());
                    });

            futures.add(future);
        }

        List<SubjectsWithQuestionsDTO> subjectsWithQuestionsDTOS = new ArrayList<>();
        try {
            for (Future<SubjectsWithQuestionsDTO> future : futures) {
                subjectsWithQuestionsDTOS.add(future.get());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Fetching was interrupted", e);
        } catch (ExecutionException e) {
            throw new LLMException("Error fetching questions",true, e.getCause());
        } finally {
            executorService.shutdown();
        }

        return subjectsWithQuestionsDTOS;
    }


    public static void main(String[] args) throws Exception {

    }
}