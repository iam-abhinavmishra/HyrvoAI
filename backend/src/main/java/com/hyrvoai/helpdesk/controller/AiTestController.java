package com.hyrvoai.helpdesk.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AiTestController {

    private final ChatClient chatClient;

    public AiTestController(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    @GetMapping("/api/ai/test")
    public String testAi() {
        return chatClient
                .prompt("Explain HyrvoAI in one simple sentence.")
                .call()
                .content();
    }
}