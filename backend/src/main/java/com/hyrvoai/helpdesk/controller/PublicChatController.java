package com.hyrvoai.helpdesk.controller;

import com.hyrvoai.helpdesk.dto.chat.ChatResponse;
import com.hyrvoai.helpdesk.dto.chat.PublicChatRequest;
import com.hyrvoai.helpdesk.entity.Company;
import com.hyrvoai.helpdesk.rag.generation.RagService;
import com.hyrvoai.helpdesk.repository.CompanyRepository;
import com.hyrvoai.helpdesk.security.PublicRateLimitService;
import com.hyrvoai.helpdesk.service.PublicChatSessionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/public/chat")
public class PublicChatController {

    private final RagService ragService;

    private final CompanyRepository companyRepository;

    private final PublicChatSessionService
            publicChatSessionService;

    private final PublicRateLimitService
            publicRateLimitService;

    public PublicChatController(
            RagService ragService,
            CompanyRepository companyRepository,
            PublicChatSessionService publicChatSessionService,
            PublicRateLimitService publicRateLimitService) {

        this.ragService = ragService;
        this.companyRepository = companyRepository;
        this.publicChatSessionService =
                publicChatSessionService;
        this.publicRateLimitService =
                publicRateLimitService;
    }

    @PostMapping
    public ChatResponse chat(
            @Valid @RequestBody PublicChatRequest request,
            HttpServletRequest httpRequest) {

        String clientKey =
                httpRequest.getRemoteAddr();

        if (!publicRateLimitService.allowRequest(clientKey)) {

            throw new ResponseStatusException(
                    HttpStatus.TOO_MANY_REQUESTS,
                    "Too many requests. Please try again later."
            );
        }

        Company company =
                companyRepository
                        .findByWidgetPublicKey(
                                request.getWidgetPublicKey()
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Invalid widget public key"
                                )
                        );

        Long companyId =
                company.getId();

        String sessionId =
                request.getSessionId();

        /*
         * Create a new anonymous session when
         * the visitor doesn't have one.
         */
        if (
                sessionId == null
                        || sessionId.isBlank()
        ) {

            sessionId =
                    publicChatSessionService
                            .createSession(
                                    companyId
                            );
        }

        /*
         * Verify that the session belongs to
         * the same company as the widget key.
         */
        PublicChatSessionService.PublicSession
                session =
                publicChatSessionService
                        .getSession(
                                sessionId,
                                companyId
                        );

        if (session == null) {

            throw new RuntimeException(
                    "Invalid public chat session"
            );
        }

        String conversationHistory =
                publicChatSessionService
                        .buildConversationHistory(
                                sessionId,
                                companyId
                        );

        /*
         * Generate answer using only this
         * company's PUBLIC documents.
         */
        RagService.RagResult result =
                ragService.answerPublicQuestion(
                        request.getMessage(),
                        conversationHistory,
                        companyId
                );

        /*
         * Store the conversation only after
         * retrieval/generation succeeds.
         */
        publicChatSessionService.addMessage(
                sessionId,
                "User",
                request.getMessage()
        );

        publicChatSessionService.addMessage(
                sessionId,
                "Assistant",
                result.getAnswer()
        );

        return new ChatResponse(
                sessionId,
                result.getAnswer(),
                result.getSources()
        );
    }
}