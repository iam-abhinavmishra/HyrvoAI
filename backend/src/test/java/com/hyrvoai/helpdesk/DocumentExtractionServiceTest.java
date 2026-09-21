package com.hyrvoai.helpdesk;

import com.hyrvoai.helpdesk.rag.ingestion.DocumentExtractionService;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DocumentExtractionServiceTest {

    @Test
    void shouldExtractTextFromInputStream() throws Exception {

        String text = "This is a HyrvoAI test document.";

        DocumentExtractionService service =
                new DocumentExtractionService();

        String extractedText = service.extractText(
                new ByteArrayInputStream(
                        text.getBytes(StandardCharsets.UTF_8)
                )
        );

        assertEquals(text, extractedText.trim());
    }
}