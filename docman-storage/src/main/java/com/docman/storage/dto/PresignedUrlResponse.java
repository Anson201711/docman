package com.docman.storage.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Presigned URL Response DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PresignedUrlResponse {

    /**
     * File ID
     */
    private String fileId;

    /**
     * Original filename
     */
    private String fileName;

    /**
     * Presigned URL for download
     */
    private String presignedUrl;

    /**
     * URL expiration time in seconds
     */
    private Integer expiresIn;

    /**
     * Expiration time as ISO string
     */
    private String expiresAt;

    /**
     * Content type
     */
    private String contentType;

    /**
     * File size
     */
    private Long fileSize;
}
