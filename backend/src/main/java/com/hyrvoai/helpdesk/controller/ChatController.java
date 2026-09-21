package com.hyrvoai.helpdesk.controller;

import com.hyrvoai.helpdesk.dto.chat.ChatMessageResponse;
import com.hyrvoai.helpdesk.dto.chat.ChatRequest;
import com.hyrvoai.helpdesk.dto.chat.ChatResponse;
import com.hyrvoai.helpdesk.dto.chat.ChatSessionResponse;
import com.hyrvoai.helpdesk.dto.chat.ChatSource;
import com.hyrvoai.helpdesk.entity.ChatSession;
import com.hyrvoai.helpdesk.entity.MessageRole;
import com.hyrvoai.helpdesk.entity.User;
import com.hyrvoai.helpdesk.rag.generation.RagService;
import com.hyrvoai.helpdesk.service.ChatService;
import com.hyrvoai.helpdesk.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;
    private final UserService userService;
    private final RagService ragService;

    public ChatController(
            ChatService chatService,
            UserService userService,
            RagService ragService) {

        this.chatService = chatService;
        this.userService = userService;
        this.ragService = ragService;
    }

    @PostMapping
    public ChatResponse chat(
            @Valid @RequestBody ChatRequest request,
            Authentication authentication) {

        User user = userService
                .findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found"));

        ChatSession session;

        /*
         * Create a new session when the request
         * does not contain a session ID.
         */
        if (request.getSessionId() == null) {

            String title =
                    chatService.generateSessionTitle(
                            request.getMessage()
                    );

            session =
                    chatService.createSession(
                            user,
                            title
                    );

        } else {

            /*
             * Existing session:
             * make sure the user owns it.
             */
            session =
                    chatService.getSessionForUser(
                            request.getSessionId(),
                            user
                    );
        }

        /*
         * RAG runs BEFORE saving the current user message.
         *
         * Therefore conversation history contains
         * only previous messages.
         */
        RagService.RagResult result =
                ragService.answerQuestion(
                        request.getMessage(),
                        session.getId(),
                        user
                );

        /*
         * Persist the current user message.
         */
        chatService.addMessage(
                session.getId(),
                MessageRole.USER,
                request.getMessage()
        );

        /*
         * Persist the assistant response.
         */
        chatService.addMessage(
                session.getId(),
                MessageRole.ASSISTANT,
                result.getAnswer()
        );

        /*
         * Return sources to the frontend.
         */
        List<ChatSource> sources =
                result.getSources();

        return new ChatResponse(
                session.getId(),
                result.getAnswer(),
                sources
        );
    }


    @GetMapping("/sessions")
    public List<ChatSessionResponse> getSessions(
            Authentication authentication
    ) {

        User user = userService
                .findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found"));

        return chatService
                .getUserSessions(user.getId())
                .stream()
                .map(session ->
                        new ChatSessionResponse(
                                session.getId(),
                                session.getTitle(),
                                session.getCreatedAt(),
                                session.getUpdatedAt()
                        )
                )
                .toList();
    }


    @GetMapping("/sessions/{sessionId}/messages")
    public List<ChatMessageResponse> getMessages(
            @PathVariable Long sessionId,
            Authentication authentication
    ) {

        User user = userService
                .findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found"));

        /*
         * Security check:
         * make sure this session belongs to
         * the authenticated user.
         */
        chatService.getSessionForUser(
                sessionId,
                user
        );

        return chatService
                .getMessages(sessionId)
                .stream()
                .map(message ->
                        new ChatMessageResponse(
                                message.getId(),
                                message.getRole(),
                                message.getContent(),
                                message.getCreatedAt()
                        )
                )
                .toList();
    }
}