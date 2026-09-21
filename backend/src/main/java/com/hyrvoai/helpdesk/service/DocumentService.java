package com.hyrvoai.helpdesk.service;

import com.hyrvoai.helpdesk.entity.Document;
import com.hyrvoai.helpdesk.entity.DocumentChunk;
import com.hyrvoai.helpdesk.rag.retrieval.VectorStoreService;
import com.hyrvoai.helpdesk.repository.DocumentChunkRepository;
import com.hyrvoai.helpdesk.repository.DocumentRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final DocumentChunkRepository documentChunkRepository;
    private final VectorStoreService vectorStoreService;

    public DocumentService(
            DocumentRepository documentRepository,
            DocumentChunkRepository documentChunkRepository,
            VectorStoreService vectorStoreService) {

        this.documentRepository = documentRepository;
        this.documentChunkRepository = documentChunkRepository;
        this.vectorStoreService = vectorStoreService;
    }

    public Document uploadDocument(
            Document document) {

        return documentRepository.save(document);
    }

    public List<Document> getActiveDocuments() {

        return documentRepository
                .findByActiveTrue();
    }

    public List<Document> getDocumentsByDepartment(
            String department) {

        return documentRepository
                .findByDepartmentAndActiveTrue(
                        department
                );
    }

    public Optional<Document> getDocumentById(
            Long id) {

        return documentRepository.findById(id);
    }

    public void deactivateDocument(
            Long id) {

        documentRepository.findById(id)
                .ifPresent(document -> {

                    document.setActive(false);

                    documentRepository.save(document);

                    vectorStoreService
                            .deleteDocumentVectors(
                                    document.getId()
                            );
                });
    }

    public void saveChunks(
            Document document,
            List<String> chunks) {

        for (int i = 0;
             i < chunks.size();
             i++) {

            String content = chunks.get(i);

            DocumentChunk chunk =
                    new DocumentChunk(
                            document,
                            content,
                            i,
                            null
                    );

            documentChunkRepository.save(chunk);

            vectorStoreService.addChunk(
                    content,
                    document,
                    i
            );
        }
    }

    /*
     * Deactivate the currently active version
     * of a document only within the same department.
     */
    public void replaceActiveDocument(
            String fileName,
            String department) {

        List<Document> existingDocuments =
                documentRepository
                        .findByFileNameAndDepartmentAndActiveTrue(
                                fileName,
                                department
                        );

        for (Document document :
                existingDocuments) {

            document.setActive(false);

            documentRepository.save(document);

            vectorStoreService
                    .deleteDocumentVectors(
                            document.getId()
                    );
        }
    }
}