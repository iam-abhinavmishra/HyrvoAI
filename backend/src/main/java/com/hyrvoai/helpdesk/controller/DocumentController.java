package com.hyrvoai.helpdesk.controller;

import com.hyrvoai.helpdesk.dto.document.DocumentResponse;
import com.hyrvoai.helpdesk.entity.Document;
import com.hyrvoai.helpdesk.entity.User;
import com.hyrvoai.helpdesk.rag.ingestion.DocumentChunkingService;
import com.hyrvoai.helpdesk.rag.ingestion.DocumentExtractionService;
import com.hyrvoai.helpdesk.service.DocumentService;
import com.hyrvoai.helpdesk.service.UserService;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
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
    private final UserService userService;

    public DocumentController(
            DocumentExtractionService extractionService,
            DocumentChunkingService chunkingService,
            DocumentService documentService,
            UserService userService) {

        this.extractionService = extractionService;
        this.chunkingService = chunkingService;
        this.documentService = documentService;
        this.userService = userService;
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
            String version,

            Authentication authentication
    ) throws IOException {

        /*
         * Get the authenticated user.
         */
        User user =
                userService
                        .findByEmail(authentication.getName())
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "User not found"
                                ));

        /*
         * Every document uploaded by an admin
         * belongs to that admin's company.
         */
        if (user.getCompany() == null) {
            throw new IllegalStateException(
                    "User is not associated with a company"
            );
        }

        Long companyId =
                user.getCompany().getId();

        /*
         * Basic validation.
         */
        if (file.isEmpty()) {
            throw new IllegalArgumentException(
                    "File cannot be empty"
            );
        }

        if (file.getSize() > 10 * 1024 * 1024) {
            throw new IllegalArgumentException(
                    "File size must be less than 10 MB"
            );
        }

        String fileName =
                file.getOriginalFilename();

        if (fileName == null
                || fileName.isBlank()) {

            throw new IllegalArgumentException(
                    "File name cannot be empty"
            );
        }

        /*
         * Validate file type.
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
                    "Only PDF and DOC/DOCX files are supported"
            );
        }

        /*
         * Defaults.
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
         * Extract the document first.
         *
         * We do NOT deactivate the previous
         * version until extraction and chunking
         * succeed.
         */
        String extractedText =
                extractionService.extractText(
                        file.getInputStream()
                );

        if (extractedText == null
                || extractedText.isBlank()) {

            throw new IllegalArgumentException(
                    "Could not extract any text from the document"
            );
        }

        /*
         * Chunk the extracted text.
         */
        List<String> chunks =
                chunkingService.chunkText(
                        extractedText
                );

        if (chunks == null
                || chunks.isEmpty()) {

            throw new IllegalArgumentException(
                    "Could not create document chunks"
            );
        }

        /*
         * Only after successful extraction
         * and chunking do we deactivate the
         * previous active version.
         *
         * IMPORTANT:
         * This is now company-specific.
         */
        documentService.replaceActiveDocument(
                fileName,
                department,
                companyId
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

        /*
         * Associate the document with the
         * authenticated user's company.
         */
        document.setCompany(
                user.getCompany()
        );

        documentService.uploadDocument(
                document
        );

        /*
         * Save chunks and create vector embeddings.
         */
        documentService.saveChunks(
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
    public List<DocumentResponse> getDocuments(
            Authentication authentication
    ) {

        User user =
                userService
                        .findByEmail(authentication.getName())
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "User not found"
                                ));

        if (user.getCompany() == null) {
            throw new IllegalStateException(
                    "User is not associated with a company"
            );
        }

        return documentService
                .getDocumentsByCompany(
                        user.getCompany().getId()
                )
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
            @PathVariable Long id,
            Authentication authentication
    ) {

        User user =
                userService
                        .findByEmail(authentication.getName())
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "User not found"
                                ));

        if (user.getCompany() == null) {
            throw new IllegalStateException(
                    "User is not associated with a company"
            );
        }

        documentService.deactivateDocumentForCompany(
                id,
                user.getCompany().getId()
        );

        return "Document deactivated successfully";
    }
}