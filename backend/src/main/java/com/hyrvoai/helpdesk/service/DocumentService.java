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

    public List<Document> getDocumentsByCompany(
            Long companyId) {

        return documentRepository
                .findByCompanyIdAndActiveTrue(
                        companyId
                );
    }

    public List<Document> getDocumentsByDepartment(
            String department,
            Long companyId) {

        return documentRepository
                .findByCompanyIdAndDepartmentAndActiveTrue(
                        companyId,
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

    public void deactivateDocumentForCompany(
            Long documentId,
            Long companyId
    ) {

        documentRepository.findById(documentId)
                .ifPresent(document -> {

                    if (document.getCompany() == null
                            || !document.getCompany()
                            .getId()
                            .equals(companyId)) {

                        throw new IllegalArgumentException(
                                "Document does not belong to your company"
                        );
                    }

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
     * of a document only within the same
     * company and department.
     */

    public void replaceActiveDocument(
            String fileName,
            String department,
            Long companyId
    ) {

        List<Document> existingDocuments =
                documentRepository
                        .findByFileNameAndDepartmentAndCompanyIdAndActiveTrue(
                                fileName,
                                department,
                                companyId
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