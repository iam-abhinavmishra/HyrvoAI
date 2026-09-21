package com.hyrvoai.helpdesk.dto.chat;

import jakarta.validation.constraints.NotBlank;

public class ChatRequest {

    private Long sessionId;

    @NotBlank
    private String message;

    public ChatRequest() {
    }

    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}