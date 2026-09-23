package com.hyrvoai.helpdesk.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "companies")
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String slug;

    private String website;

    private String logoUrl;

    @Column(name = "widget_public_key", nullable = false, unique = true)
    private String widgetPublicKey;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public Company() {
    }

    public Company(
            String name,
            String slug
    ) {
        this.name = name;
        this.slug = slug;
        this.createdAt = LocalDateTime.now();
        this.widgetPublicKey = generateWidgetPublicKey();
    }

    @PrePersist
    protected void onCreate() {

        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }

        if (widgetPublicKey == null
                || widgetPublicKey.isBlank()) {

            widgetPublicKey = generateWidgetPublicKey();
        }
    }

    private String generateWidgetPublicKey() {

        return "hyrvo_pub_" +
                UUID.randomUUID()
                        .toString()
                        .replace("-", "");
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public String getWidgetPublicKey() {
        return widgetPublicKey;
    }

    public void setWidgetPublicKey(String widgetPublicKey) {
        this.widgetPublicKey = widgetPublicKey;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}