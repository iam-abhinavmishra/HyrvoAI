package com.hyrvoai.helpdesk.dto.chat;

import java.util.List;

public class ChatResponse {

    private Long sessionId;
    private String answer;
    private List<ChatSource> sources;

    public ChatResponse(
            Long sessionId,
            String answer,
            List<ChatSource> sources) {

        this.sessionId = sessionId;
        this.answer = answer;
        this.sources = sources;
    }

    public Long getSessionId() {
        return sessionId;
    }

    public String getAnswer() {
        return answer;
    }

    public List<ChatSource> getSources() {
        return sources;
    }
}