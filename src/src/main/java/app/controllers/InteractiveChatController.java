
        package app.controllers;

import app.exceptions.LLMException;
import app.service.InteractiveChat;
import io.javalin.http.Context;

import java.util.List;
import java.util.Map;

        /**
         * Controller responsible for handling HTTP requests related to interactive
         * AI chat.
         *
         * Receives conversation messages from the client, passes them to the chat
         * service, and returns the AI-generated reply as JSON.
         */
        public class InteractiveChatController {

            private final InteractiveChat chatService;

            /**
             * Constructs an InteractiveChatController with the specified chat service.
             *
             * @param chatService the service responsible for processing AI chat requests
             */
            public InteractiveChatController(InteractiveChat chatService) {
                this.chatService = chatService;
            }

            /**
             * Processes an AI chat request.
             *
             * Validates that the request contains conversation messages, forwards
             * them to the chat service, and returns the generated reply.
             *
             * Returns HTTP 400 if no messages are provided and HTTP 502 if the
             * AI service fails to respond.
             *
             * @param ctx the Javalin context containing the request body and
             *            the HTTP response
             */
            public void chat(Context ctx) {
                ChatRequest request = ctx.bodyAsClass(ChatRequest.class);

                if (request.contents() == null ||
                        request.contents().isEmpty()) {
                    ctx.status(400).json(
                            Map.of("error", "Messages are required")
                    );
                    return;
                }

                try {
                    String reply = chatService.chat(request.contents());

                    ctx.json(Map.of("reply", reply));

                } catch (LLMException e) {
                    ctx.status(502).json(
                            Map.of("error", "The AI service is unavailable")
                    );
                }
            }

            /**
             * Represents the request body for an interactive AI chat request.
             *
             * @param contents the conversation messages supplied to the AI service,
             *                 including their roles and message content
             */
            public record ChatRequest(
                    List<Map<String, Object>> contents
            ) {}
        }
