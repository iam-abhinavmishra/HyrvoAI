package com.hyrvoai.helpdesk.rag.ingestion;

import org.apache.tika.Tika;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;

@Service
public class DocumentExtractionService {

    private final Tika tika = new Tika();

    public String extractText(InputStream inputStream)
            throws IOException {

        try {
            return tika.parseToString(inputStream);
        } catch (org.apache.tika.exception.TikaException e) {
            throw new IOException("Failed to extract text from document", e);
        }
    }
}