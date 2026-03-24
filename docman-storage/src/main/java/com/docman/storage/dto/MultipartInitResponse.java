package com.docman.storage.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Multipart Init Response DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MultipartInitResponse {

    /**
     * Upload ID from MinIO
     */
    private String uploadId;

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
     * Total file size in bytes
     */
    private Long totalSize;

    /**
     * Part size in bytes
     */
    private Long partSize;

    /**
     * Total number of parts
     */
    private Integer totalParts;

    /**
     * MIME type
     */
    private String contentType;
}
