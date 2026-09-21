package com.hyrvoai.helpdesk.dto.chat;

public class ChatSource {

    private String document;
    private String title;
    private String version;
    private Long documentId;
    private Integer chunkIndex;

    public ChatSource(
            String document,
            String title,
            String version,
            Long documentId,
            Integer chunkIndex) {

        this.document = document;
        this.title = title;
        this.version = version;
        this.documentId = documentId;
        this.chunkIndex = chunkIndex;
    }

    public String getDocument() {
        return document;
    }

    public String getTitle() {
        return title;
    }

    public String getVersion() {
        return version;
    }

    public Long getDocumentId() {
        return documentId;
    }

    public Integer getChunkIndex() {
        return chunkIndex;
    }
}