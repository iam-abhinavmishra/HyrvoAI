package com.hyrvoai.helpdesk.service;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PublicChatSessionService {

    private static final int MAX_MESSAGES = 20;

    private final Map<String, PublicSession> sessions =
            new ConcurrentHashMap<>();

    public String createSession(Long companyId) {

        if (companyId == null) {
            throw new IllegalArgumentException(
                    "Company ID cannot be null"
            );
        }

        String sessionId =
                UUID.randomUUID().toString();

        sessions.put(
                sessionId,
                new PublicSession(companyId)
        );

        return sessionId;
    }

    public PublicSession getSession(
            String sessionId,
            Long companyId) {

        if (sessionId == null
                || sessionId.isBlank()) {
            return null;
        }

        PublicSession session =
                sessions.get(sessionId);

        if (session == null) {
            return null;
        }

        if (!session.getCompanyId()
                .equals(companyId)) {

            return null;
        }

        return session;
    }

    public void addMessage(
            String sessionId,
            String role,
            String content) {

        PublicSession session =
                sessions.get(sessionId);

        if (session == null) {
            return;
        }

        synchronized (session) {

            session.messages.add(
                    new PublicMessage(
                            role,
                            content,
                            Instant.now()
                    )
            );

            while (
                    session.messages.size()
                            > MAX_MESSAGES
            ) {
                session.messages.remove(0);
            }
        }
    }

    public String buildConversationHistory(
            String sessionId,
            Long companyId) {

        PublicSession session =
                getSession(
                        sessionId,
                        companyId
                );

        if (session == null) {
            return "";
        }

        StringBuilder history =
                new StringBuilder();

        synchronized (session) {

            for (
                    PublicMessage message
                    : session.messages
            ) {

                history
                        .append(
                                message.role()
                        )
                        .append(": ")
                        .append(
                                message.content()
                        )
                        .append("\n");
            }
        }

        return history.toString();
    }

    public record PublicMessage(
            String role,
            String content,
            Instant createdAt
    ) {}

    public static class PublicSession {

        private final Long companyId;

        private final List<PublicMessage>
                messages =
                new ArrayList<>();

        public PublicSession(
                Long companyId) {

            this.companyId =
                    companyId;
        }

        public Long getCompanyId() {
            return companyId;
        }
    }
}