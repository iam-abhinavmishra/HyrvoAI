package com.hyrvoai.helpdesk.service;

import com.hyrvoai.helpdesk.entity.ChatMessage;
import com.hyrvoai.helpdesk.entity.ChatSession;
import com.hyrvoai.helpdesk.entity.MessageRole;
import com.hyrvoai.helpdesk.entity.User;
import com.hyrvoai.helpdesk.repository.ChatMessageRepository;
import com.hyrvoai.helpdesk.repository.ChatSessionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChatService {

    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;

    public ChatService(
            ChatSessionRepository chatSessionRepository,
            ChatMessageRepository chatMessageRepository) {

        this.chatSessionRepository = chatSessionRepository;
        this.chatMessageRepository = chatMessageRepository;
    }

    public ChatSession createSession(
            User user,
            String title) {

        if (title == null || title.isBlank()) {
            title = "New Chat";
        }

        ChatSession session =
                new ChatSession(user, title);

        return chatSessionRepository.save(session);
    }

    public String generateSessionTitle(String question) {

        if (question == null || question.isBlank()) {
            return "New Chat";
        }

        String cleanedQuestion =
                question.trim()
                        .replaceAll("\\s+", " ");

        if (cleanedQuestion.length() <= 45) {
            return cleanedQuestion;
        }

        return cleanedQuestion.substring(0, 42)
                + "...";
    }
    public List<ChatSession> getUserSessions(
            Long userId) {

        return chatSessionRepository
                .findByUserIdOrderByUpdatedAtDesc(userId);
    }

    public ChatSession getSessionForUser(
            Long sessionId,
            User user) {

        ChatSession session =
                chatSessionRepository.findById(sessionId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Chat session not found"));

        if (!session.getUser().getId().equals(user.getId())) {
            throw new RuntimeException(
                    "You do not have access to this chat session");
        }

        return session;
    }

    public ChatMessage addMessage(
            Long sessionId,
            MessageRole role,
            String content) {

        ChatSession session =
                chatSessionRepository.findById(sessionId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Chat session not found"));

        ChatMessage message =
                new ChatMessage(role, content);

        message.setChatSession(session);

        return chatMessageRepository.save(message);
    }

    public List<ChatMessage> getMessages(
            Long sessionId) {

        return chatMessageRepository
                .findByChatSessionIdOrderByCreatedAtAsc(
                        sessionId);
    }

    public String buildConversationHistory(
            Long sessionId) {

        if (sessionId == null) {
            return "";
        }

        List<ChatMessage> messages =
                getMessages(sessionId);

        if (messages.isEmpty()) {
            return "";
        }

        StringBuilder history =
                new StringBuilder();

        for (ChatMessage message : messages) {

            String role =
                    message.getRole() == MessageRole.USER
                            ? "User"
                            : "Assistant";

            history.append(role)
                    .append(": ")
                    .append(message.getContent())
                    .append("\n");
        }

        return history.toString();
    }
}