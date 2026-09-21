package com.hyrvoai.helpdesk.controller;

import com.hyrvoai.helpdesk.dto.document.DocumentResponse;
import com.hyrvoai.helpdesk.entity.Document;
import com.hyrvoai.helpdesk.rag.ingestion.DocumentChunkingService;
import com.hyrvoai.helpdesk.rag.ingestion.DocumentExtractionService;
import com.hyrvoai.helpdesk.service.DocumentService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/admin/documents")
public class DocumentController {

    private final DocumentExtractionService extractionService;
    private final DocumentChunkingService chunkingService;
    private final DocumentService documentService;

    public DocumentController(
            DocumentExtractionService extractionService,
            DocumentChunkingService chunkingService,
            DocumentService documentService) {

        this.extractionService = extractionService;
        this.chunkingService = chunkingService;
        this.documentService = documentService;
    }

    @PostMapping(
            value = "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public String uploadDocument(
            @RequestParam("file")
            MultipartFile file,

            @RequestParam(
                    value = "title",
                    required = false)
            String title,

            @RequestParam(
                    value = "department",
                    required = false)
            String department,

            @RequestParam(
                    value = "version",
                    required = false,
                    defaultValue = "1.0")
            String version
    ) throws IOException {

        /*
         * Basic validation
         */
        if (file.isEmpty()) {
            throw new IllegalArgumentException(
                    "File cannot be empty");
        }

        if (file.getSize() > 10 * 1024 * 1024) {
            throw new IllegalArgumentException(
                    "File size must be less than 10 MB");
        }

        String fileName =
                file.getOriginalFilename();

        if (fileName == null
                || fileName.isBlank()) {

            throw new IllegalArgumentException(
                    "File name cannot be empty");
        }

        /*
         * Validate file type
         */
        String contentType =
                file.getContentType();

        if (contentType == null
                || (!contentType.equals(
                "application/pdf")
                && !contentType.equals(
                "application/msword")
                && !contentType.equals(
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document"))) {

            throw new IllegalArgumentException(
                    "Only PDF and DOC/DOCX files are supported");
        }

        /*
         * Defaults
         */
        if (title == null
                || title.isBlank()) {

            title = fileName;
        }

        if (department == null
                || department.isBlank()) {

            department = "GENERAL";
        }

        if (version == null
                || version.isBlank()) {

            version = "1.0";
        }

        /*
         * IMPORTANT:
         *
         * Extract and chunk the new document FIRST.
         *
         * We do NOT deactivate the old document yet.
         *
         * If extraction fails, the existing document
         * remains active.
         */
        String extractedText =
                extractionService.extractText(
                        file.getInputStream()
                );

        if (extractedText == null
                || extractedText.isBlank()) {

            throw new IllegalArgumentException(
                    "Could not extract any text from the document");
        }

        List<String> chunks =
                chunkingService.chunkText(
                        extractedText
                );

        if (chunks == null
                || chunks.isEmpty()) {

            throw new IllegalArgumentException(
                    "Could not create document chunks");
        }

        /*
         * Only after successful extraction and
         * chunking do we deactivate the previous
         * active version.
         */
        documentService
                .replaceActiveDocument(
                        fileName,
                        department
                );

        /*
         * Create the new document.
         */
        Document document =
                new Document(
                        fileName,
                        contentType,
                        title,
                        department,
                        version
                );

        documentService
                .uploadDocument(document);

        /*
         * Save chunks and create vector embeddings.
         */
        documentService
                .saveChunks(
                        document,
                        chunks
                );

        return "Document uploaded successfully. "
                + "Created "
                + chunks.size()
                + " chunks. "
                + "Version: "
                + version
                + ". Department: "
                + department
                + ".";
    }


    @GetMapping
    public List<DocumentResponse> getDocuments() {

        return documentService
                .getActiveDocuments()
                .stream()
                .map(document ->
                        new DocumentResponse(
                                document.getId(),
                                document.getFileName(),
                                document.getFileType(),
                                document.getTitle(),
                                document.getDepartment(),
                                document.getVersion(),
                                document.getUploadedAt(),
                                document.isActive()
                        )
                )
                .toList();
    }


    @DeleteMapping("/{id}")
    public String deactivateDocument(
            @PathVariable Long id) {

        documentService
                .deactivateDocument(id);

        return "Document deactivated successfully";
    }
}