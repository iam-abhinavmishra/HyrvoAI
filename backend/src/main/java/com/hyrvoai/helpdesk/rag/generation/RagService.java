package com.hyrvoai.helpdesk.rag.generation;

import com.hyrvoai.helpdesk.dto.chat.ChatSource;
import com.hyrvoai.helpdesk.rag.retrieval.DocumentRetrievalService;
import com.hyrvoai.helpdesk.service.ChatService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import com.hyrvoai.helpdesk.entity.User;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RagService {

    private final DocumentRetrievalService retrievalService;
    private final ChatClient chatClient;
    private final ChatService chatService;

    public RagService(
            DocumentRetrievalService retrievalService,
            ChatClient.Builder chatClientBuilder,
            ChatService chatService) {

        this.retrievalService = retrievalService;
        this.chatClient = chatClientBuilder.build();
        this.chatService = chatService;
    }

    public RagResult answerQuestion(
            String question,
            Long sessionId,
            User user) {

        // -----------------------------------------
        // 1. Get previous conversation
        // -----------------------------------------

        String conversationHistory = "";

        if (sessionId != null) {
            conversationHistory =
                    chatService.buildConversationHistory(sessionId);
        }

        // -----------------------------------------
        // 2. Rewrite follow-up question
        // -----------------------------------------

        String searchQuery =
                rewriteQuestion(
                        question,
                        conversationHistory
                );

        System.out.println();
        System.out.println("========== RAG DEBUG ==========");
        System.out.println("Original question: " + question);
        System.out.println("Search query: " + searchQuery);
        System.out.println("================================");

        // -----------------------------------------
        // 3. Retrieve relevant documents
        // -----------------------------------------

        List<Document> relevantDocuments =
                retrievalService.search(
                        searchQuery,
                        user
                );

        System.out.println(
                "Retrieved documents: "
                        + relevantDocuments.size()
        );

        for (Document document : relevantDocuments) {

            System.out.println("--------------------------------");
            System.out.println(
                    "Document ID: "
                            + document.getMetadata()
                            .get("documentId")
            );

            System.out.println(
                    "Chunk: "
                            + document.getMetadata()
                            .get("chunkIndex")
            );

            System.out.println(
                    "Text:\n"
                            + document.getText()
            );
        }

        System.out.println("================================");

        // -----------------------------------------
        // 4. Nothing retrieved
        // -----------------------------------------

        if (relevantDocuments.isEmpty()) {

            return new RagResult(
                    "I couldn't find that information in the company documents.",
                    List.of()
            );
        }

        // -----------------------------------------
        // 5. Build context
        // -----------------------------------------

        String context =
                relevantDocuments.stream()
                        .map(Document::getText)
                        .filter(text ->
                                text != null && !text.isBlank())
                        .collect(Collectors.joining(
                                "\n\n---\n\n"
                        ));

        // -----------------------------------------
        // 6. Generate final answer
        // -----------------------------------------

        String userPrompt = """
                Answer the user's question using ONLY the
                document context provided below.

                DOCUMENT CONTEXT:
                -----------------
                %s
                -----------------

                CONVERSATION HISTORY:
                ---------------------
                %s
                ---------------------

                USER QUESTION:
                %s

                IMPORTANT:

                - If the answer is present in the document context,
                  give the answer directly.
                - Do not say the information is unavailable when
                  it is explicitly present in the context.
                - Do not use outside knowledge.
                - Do not guess.
                - Conversation history is only for understanding
                  references such as "it", "that", "the previous one",
                  or "what about".
                - If the answer genuinely cannot be found in the
                  document context, say:
                  "I couldn't find that information in the company documents."
                - Keep the answer concise.
                """.formatted(
                context,
                conversationHistory,
                question
        );

        String answer =
                chatClient
                        .prompt()
                        .system("""
                                You are HyrvoAI, an internal company
                                document assistant.

                                Answer questions using the supplied
                                document context.

                                Never invent information.
                                """)
                        .user(userPrompt)
                        .call()
                        .content();

        if (answer == null || answer.isBlank()) {

            answer =
                    "I couldn't generate an answer from the company documents.";
        }

        // -----------------------------------------
        // 7. Sources
        // -----------------------------------------

        List<ChatSource> sources =
                relevantDocuments.stream()
                        .map(this::createSource)
                        .toList();

        return new RagResult(
                answer,
                sources
        );
    }

    private String rewriteQuestion(
            String question,
            String conversationHistory) {

        // No previous conversation.
        if (conversationHistory == null
                || conversationHistory.isBlank()) {

            return question;
        }

        String rewritePrompt = """
                Rewrite the current question into a standalone
                search query.

                Use the conversation history to understand references.

                Do NOT answer the question.

                Return ONLY the rewritten search query.

                Conversation history:
                %s

                Current question:
                %s
                """.formatted(
                conversationHistory,
                question
        );

        String rewrittenQuestion =
                chatClient
                        .prompt()
                        .user(rewritePrompt)
                        .call()
                        .content();

        if (rewrittenQuestion == null
                || rewrittenQuestion.isBlank()) {

            return question;
        }

        return rewrittenQuestion.trim();
    }

    private ChatSource createSource(
            Document document) {

        Object documentId =
                document.getMetadata()
                        .get("documentId");

        Object fileName =
                document.getMetadata()
                        .get("fileName");

        Object title =
                document.getMetadata()
                        .get("title");

        Object version =
                document.getMetadata()
                        .get("version");

        Object chunkIndex =
                document.getMetadata()
                        .get("chunkIndex");

        return new ChatSource(
                fileName != null
                        ? fileName.toString()
                        : null,

                title != null
                        ? title.toString()
                        : null,

                version != null
                        ? version.toString()
                        : null,

                documentId != null
                        ? Long.valueOf(
                        documentId.toString())
                        : null,

                chunkIndex != null
                        ? Integer.valueOf(
                        chunkIndex.toString())
                        : null
        );
    }

    public static class RagResult {

        private final String answer;
        private final List<ChatSource> sources;

        public RagResult(
                String answer,
                List<ChatSource> sources) {

            this.answer = answer;
            this.sources = sources;
        }

        public String getAnswer() {
            return answer;
        }

        public List<ChatSource> getSources() {
            return sources;
        }
    }
}