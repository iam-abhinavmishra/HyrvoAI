package com.hyrvoai.helpdesk.repository;

import com.hyrvoai.helpdesk.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentRepository
        extends JpaRepository<Document, Long> {

    List<Document> findByActiveTrue();

    List<Document> findByDepartmentAndActiveTrue(
            String department
    );

    List<Document> findByFileNameAndActiveTrue(
            String fileName
    );

    List<Document> findByFileNameAndDepartmentAndActiveTrue(
            String fileName,
            String department
    );
}