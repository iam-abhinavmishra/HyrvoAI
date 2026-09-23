package com.hyrvoai.helpdesk.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "documents")
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private String fileType;

    @Column(nullable = false)
    private String title;

    private String department;

    private String version;

    private LocalDateTime uploadedAt;

    @Column(nullable = false)
    private boolean active = true;

    /*
     * PUBLIC:
     * Available to unauthenticated/public users.
     *
     * EMPLOYEE:
     * Available only to authenticated employees
     * belonging to the same company.
     *
     * Existing documents may temporarily contain
     * NULL while the database is being migrated.
     * NULL is treated as PUBLIC by getAccessLevel().
     */
    @Column(name = "access_level")
    private String accessLevel = "PUBLIC";

    public Document() {
    }

    public Document(
            String fileName,
            String fileType,
            String title,
            String department,
            String version
    ) {
        this.fileName = fileName;
        this.fileType = fileType;
        this.title = title;
        this.department = department;
        this.version = version;
        this.uploadedAt = LocalDateTime.now();
        this.active = true;
        this.accessLevel = "PUBLIC";
    }

    @PrePersist
    protected void onCreate() {

        if (uploadedAt == null) {
            uploadedAt = LocalDateTime.now();
        }

        if (accessLevel == null || accessLevel.isBlank()) {
            accessLevel = "PUBLIC";
        }
    }

    @PreUpdate
    protected void onUpdate() {

        if (accessLevel == null || accessLevel.isBlank()) {
            accessLevel = "PUBLIC";
        }
    }

    public Long getId() {
        return id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public String getAccessLevel() {

        if (accessLevel == null || accessLevel.isBlank()) {
            return "PUBLIC";
        }

        return accessLevel.toUpperCase();
    }

    public void setAccessLevel(String accessLevel) {

        if (accessLevel == null || accessLevel.isBlank()) {
            this.accessLevel = "PUBLIC";
            return;
        }

        String normalized = accessLevel.toUpperCase();

        if (!normalized.equals("PUBLIC")
                && !normalized.equals("EMPLOYEE")) {

            throw new IllegalArgumentException(
                    "Access level must be PUBLIC or EMPLOYEE"
            );
        }

        this.accessLevel = normalized;
    }
}