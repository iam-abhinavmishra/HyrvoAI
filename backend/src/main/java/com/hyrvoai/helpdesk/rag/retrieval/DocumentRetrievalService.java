package com.hyrvoai.helpdesk.rag.retrieval;

import com.hyrvoai.helpdesk.entity.User;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DocumentRetrievalService {

    private final VectorStore vectorStore;

    public DocumentRetrievalService(
            VectorStore vectorStore) {

        this.vectorStore = vectorStore;
    }

    public List<Document> search(
            String question,
            User user) {

        String filterExpression;

        if ("ADMIN".equalsIgnoreCase(
                user.getRole())) {

            filterExpression =
                    "active == true";

        } else {

            String department =
                    user.getDepartment();

            if (department == null
                    || department.isBlank()) {

                department = "GENERAL";
            }

            filterExpression =
                    "active == true && " +
                            "(department == 'GENERAL' || " +
                            "department == '" +
                            escapeFilterValue(department) +
                            "')";
        }

        SearchRequest searchRequest =
                SearchRequest.builder()
                        .query(question)
                        .topK(3)
                        .similarityThreshold(0.40)
                        .filterExpression(
                                filterExpression
                        )
                        .build();

        return vectorStore
                .similaritySearch(searchRequest);
    }

    private String escapeFilterValue(
            String value) {

        return value.replace("'", "''");
    }
}