package com.hyrvoai.helpdesk.rag.retrieval;

import com.hyrvoai.helpdesk.entity.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VectorStoreService {

    private final VectorStore vectorStore;

    public VectorStoreService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    public void addChunk(
            String content,
            Document document,
            Integer chunkIndex) {

        org.springframework.ai.document.Document vectorDocument =
                new org.springframework.ai.document.Document(content);

        vectorDocument.getMetadata().put(
                "documentId",
                document.getId()
        );

        vectorDocument.getMetadata().put(
                "fileName",
                document.getFileName()
        );

        vectorDocument.getMetadata().put(
                "title",
                document.getTitle()
        );

        vectorDocument.getMetadata().put(
                "version",
                document.getVersion()
        );

        vectorDocument.getMetadata().put(
                "chunkIndex",
                chunkIndex
        );

        vectorDocument.getMetadata().put(
                "active",
                document.isActive()
        );

        String department = document.getDepartment();

        if (department == null || department.isBlank()) {
            department = "GENERAL";
        }

        vectorDocument.getMetadata().put(
                "department",
                department
        );

        vectorStore.add(List.of(vectorDocument));
    }

    public void deleteDocumentVectors(Long documentId) {

        vectorStore.delete(
                "documentId == " + documentId
        );
    }
}