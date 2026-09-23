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

    /*
     * Authenticated user search.
     *
     * ADMIN:
     *   Can retrieve every active document belonging
     *   to their company.
     *
     * EMPLOYEE:
     *   Can retrieve PUBLIC + EMPLOYEE documents
     *   belonging to their company and allowed department.
     */
    public List<Document> search(
            String question,
            User user) {

        if (user == null) {
            throw new IllegalArgumentException(
                    "User cannot be null"
            );
        }

        if (user.getCompany() == null) {
            throw new IllegalStateException(
                    "User is not associated with a company"
            );
        }

        Long companyId =
                user.getCompany().getId();

        String filterExpression;

        if ("ADMIN".equalsIgnoreCase(
                user.getRole())) {

            filterExpression =
                    "active == true"
                            + " && companyId == "
                            + companyId;

        } else {

            String department =
                    user.getDepartment();

            if (department == null
                    || department.isBlank()) {

                department = "GENERAL";
            }

            filterExpression =
                    "active == true"
                            + " && companyId == "
                            + companyId
                            + " && (accessLevel == 'PUBLIC'"
                            + " || accessLevel == 'EMPLOYEE')"
                            + " && (department == 'GENERAL'"
                            + " || department == '"
                            + escapeFilterValue(department)
                            + "')";
        }

        return performSearch(
                question,
                filterExpression
        );
    }

    /*
     * Public visitor search.
     *
     * No User object is accepted here.
     *
     * Therefore a public visitor can ONLY retrieve
     * PUBLIC documents from the specified company.
     */
    public List<Document> searchPublic(
            String question,
            Long companyId) {

        if (companyId == null) {
            throw new IllegalArgumentException(
                    "Company ID cannot be null"
            );
        }

        String filterExpression =
                "active == true"
                        + " && companyId == "
                        + companyId
                        + " && accessLevel == 'PUBLIC'";

        return performSearch(
                question,
                filterExpression
        );
    }

    private List<Document> performSearch(
            String question,
            String filterExpression) {

        if (question == null
                || question.isBlank()) {

            throw new IllegalArgumentException(
                    "Question cannot be empty"
            );
        }

        SearchRequest searchRequest =
                SearchRequest.builder()
                        .query(question)
                        .topK(5)
                        .similarityThreshold(0.3)
                        .filterExpression(
                                filterExpression
                        )
                        .build();

        return vectorStore.similaritySearch(
                searchRequest
        );
    }

    private String escapeFilterValue(
            String value) {

        return value.replace(
                "'",
                "''"
        );
    }
}