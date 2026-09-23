package com.hyrvoai.helpdesk.rag.retrieval;

import com.hyrvoai.helpdesk.entity.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class VectorStoreService {

    private final VectorStore vectorStore;

    public VectorStoreService(
            VectorStore vectorStore
    ) {
        this.vectorStore = vectorStore;
    }

    public void addChunk(
            String content,
            Document document,
            Integer chunkIndex
    ) {

        org.springframework.ai.document.Document vectorDocument =
                new org.springframework.ai.document.Document(
                        content,
                        Map.of(
                                "documentId",
                                document.getId(),

                                "fileName",
                                document.getFileName(),

                                "title",
                                document.getTitle(),

                                "version",
                                document.getVersion(),

                                "chunkIndex",
                                chunkIndex,

                                "active",
                                document.isActive(),

                                "department",
                                getDepartment(document),

                                "companyId",
                                document.getCompany().getId(),

                                "accessLevel",
                                document.getAccessLevel()
                        )
                );

        vectorStore.add(
                List.of(vectorDocument)
        );
    }

    private String getDepartment(
            Document document
    ) {

        String department =
                document.getDepartment();

        if (department == null
                || department.isBlank()) {

            return "GENERAL";
        }

        return department;
    }

    public void deleteDocumentVectors(
            Long documentId
    ) {

        vectorStore.delete(
                "documentId == " + documentId
        );
    }
}