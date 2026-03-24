package com.docman.storage.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * File Upload Response DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadResponse {

    /**
     * File ID
     */
    private String fileId;

    /**
     * Original filename
     */
    private String fileName;

    /**
     * File extension
     */
    private String extension;

    /**
     * MIME type
     */
    private String contentType;

    /**
     * File size in bytes
     */
    private Long fileSize;

    /**
     * MinIO object name
     */
    private String objectName;

    /**
     * Download URL
     */
    private String downloadUrl;

    /**
     * Upload status
     */
    private String status;
}
