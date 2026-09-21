package com.hyrvoai.helpdesk;



import com.hyrvoai.helpdesk.rag.ingestion.DocumentChunkingService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DocumentChunkingServiceTest {

    @Test
    void shouldSplitLongTextIntoChunks() {

        DocumentChunkingService service =
                new DocumentChunkingService();

        String text = "A".repeat(2500);

        List<String> chunks = service.chunkText(text);

        assertTrue(chunks.size() > 1);
        assertEquals(1000, chunks.get(0).length());
        assertEquals(1000, chunks.get(1).length());
    }
}