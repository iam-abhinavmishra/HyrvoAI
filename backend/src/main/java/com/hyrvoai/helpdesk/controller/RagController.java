package com.hyrvoai.helpdesk.controller;

import com.hyrvoai.helpdesk.entity.User;
import com.hyrvoai.helpdesk.rag.generation.RagService;
import com.hyrvoai.helpdesk.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rag")
public class RagController {

    private final RagService ragService;
    private final UserService userService;

    public RagController(
            RagService ragService,
            UserService userService) {

        this.ragService = ragService;
        this.userService = userService;
    }

    @GetMapping("/ask")
    public RagService.RagResult ask(
            @RequestParam String question,
            Authentication authentication) {

        User user = userService
                .findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        return ragService.answerQuestion(
                question,
                null,
                user
        );
    }
}