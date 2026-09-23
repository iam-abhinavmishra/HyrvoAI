package com.hyrvoai.helpdesk.dto.chat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class PublicChatRequest {

    @Size(
            max = 1000,
            message = "Message cannot exceed 1000 characters"
    )
    @NotBlank(message = "Message cannot be empty")
    private String message;

    private String sessionId;

    @NotBlank(message = "Widget public key is required")
    private String widgetPublicKey;

    public PublicChatRequest() {
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(
            String message) {

        this.message = message;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(
            String sessionId) {

        this.sessionId = sessionId;
    }

    public String getWidgetPublicKey() {
        return widgetPublicKey;
    }

    public void setWidgetPublicKey(
            String widgetPublicKey) {

        this.widgetPublicKey =
                widgetPublicKey;
    }
}