package com.hyrvoai.helpdesk.repository;

import com.hyrvoai.helpdesk.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentRepository
        extends JpaRepository<Document, Long> {

    List<Document> findByActiveTrue();

    List<Document> findByCompanyIdAndActiveTrue(
            Long companyId
    );

    List<Document> findByCompanyIdAndDepartmentAndActiveTrue(
            Long companyId,
            String department
    );

    List<Document>
    findByFileNameAndDepartmentAndCompanyIdAndActiveTrue(
            String fileName,
            String department,
            Long companyId
    );
}