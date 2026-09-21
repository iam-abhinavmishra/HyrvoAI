package com.hyrvoai.helpdesk.controller;

import com.hyrvoai.helpdesk.entity.User;
import com.hyrvoai.helpdesk.rag.retrieval.DocumentRetrievalService;
import com.hyrvoai.helpdesk.service.UserService;
import org.springframework.ai.document.Document;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rag")
public class RagTestController {

    private final DocumentRetrievalService retrievalService;
    private final UserService userService;

    public RagTestController(
            DocumentRetrievalService retrievalService,
            UserService userService) {

        this.retrievalService = retrievalService;
        this.userService = userService;
    }

    @GetMapping("/search")
    public List<Document> search(
            @RequestParam String question,
            Authentication authentication) {

        User user = userService
                .findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        return retrievalService.search(
                question,
                user
        );
    }
}