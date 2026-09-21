package com.hyrvoai.helpdesk.dto.chat;

import com.hyrvoai.helpdesk.entity.MessageRole;

import java.time.LocalDateTime;

public class ChatMessageResponse {

    private Long id;
    private MessageRole role;
    private String content;
    private LocalDateTime createdAt;

    public ChatMessageResponse() {
    }

    public ChatMessageResponse(
            Long id,
            MessageRole role,
            String content,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.role = role;
        this.content = content;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public MessageRole getRole() {
        return role;
    }

    public String getContent() {
        return content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}