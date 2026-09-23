package com.hyrvoai.helpdesk.dto.chat;

import java.util.List;

public class ChatResponse {

    private String sessionId;
    private String answer;
    private List<ChatSource> sources;

    /*
     * Used by authenticated chat.
     * The database session ID is Long,
     * so convert it to String for the API response.
     */
    public ChatResponse(
            Long sessionId,
            String answer,
            List<ChatSource> sources) {

        this.sessionId =
                sessionId != null
                        ? sessionId.toString()
                        : null;

        this.answer = answer;
        this.sources = sources;
    }

    /*
     * Used by anonymous public chat.
     * Public session IDs are UUID Strings.
     */
    public ChatResponse(
            String sessionId,
            String answer,
            List<ChatSource> sources) {

        this.sessionId = sessionId;
        this.answer = answer;
        this.sources = sources;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getAnswer() {
        return answer;
    }

    public List<ChatSource> getSources() {
        return sources;
    }
}