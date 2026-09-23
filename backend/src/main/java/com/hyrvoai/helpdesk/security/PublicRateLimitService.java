package com.hyrvoai.helpdesk.security;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PublicRateLimitService {

    private static final int MAX_REQUESTS = 20;

    private static final long WINDOW_SECONDS = 60;

    private final Map<String, RateLimitEntry> clients =
            new ConcurrentHashMap<>();

    public boolean allowRequest(String clientKey) {

        long now = Instant.now().getEpochSecond();

        RateLimitEntry entry =
                clients.computeIfAbsent(
                        clientKey,
                        key -> new RateLimitEntry(
                                now,
                                0
                        )
                );

        synchronized (entry) {

            if (
                    now - entry.windowStart
                            >= WINDOW_SECONDS
            ) {
                entry.windowStart = now;
                entry.requestCount = 0;
            }

            if (
                    entry.requestCount
                            >= MAX_REQUESTS
            ) {
                return false;
            }

            entry.requestCount++;

            return true;
        }
    }

    private static class RateLimitEntry {

        private long windowStart;

        private int requestCount;

        private RateLimitEntry(
                long windowStart,
                int requestCount
        ) {
            this.windowStart = windowStart;
            this.requestCount = requestCount;
        }
    }
}