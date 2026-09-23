package com.hyrvoai.helpdesk.repository;

import com.hyrvoai.helpdesk.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CompanyRepository
        extends JpaRepository<Company, Long> {

    Optional<Company> findBySlug(String slug);

    boolean existsBySlug(String slug);

    Optional<Company> findByWidgetPublicKey(
            String widgetPublicKey
    );

    boolean existsByWidgetPublicKey(
            String widgetPublicKey
    );
}