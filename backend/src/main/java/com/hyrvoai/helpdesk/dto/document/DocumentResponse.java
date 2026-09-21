package com.hyrvoai.helpdesk.dto.document;

import java.time.LocalDateTime;

public class DocumentResponse {

    private Long id;
    private String fileName;
    private String fileType;
    private String title;
    private String department;
    private String version;
    private LocalDateTime uploadedAt;
    private boolean active;

    public DocumentResponse() {
    }

    public DocumentResponse(
            Long id,
            String fileName,
            String fileType,
            String title,
            String department,
            String version,
            LocalDateTime uploadedAt,
            boolean active
    ) {
        this.id = id;
        this.fileName = fileName;
        this.fileType = fileType;
        this.title = title;
        this.department = department;
        this.version = version;
        this.uploadedAt = uploadedAt;
        this.active = active;
    }

    public Long getId() {
        return id;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFileType() {
        return fileType;
    }

    public String getTitle() {
        return title;
    }

    public String getDepartment() {
        return department;
    }

    public String getVersion() {
        return version;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public boolean isActive() {
        return active;
    }
}