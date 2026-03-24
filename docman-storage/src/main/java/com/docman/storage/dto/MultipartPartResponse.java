package com.docman.storage.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Multipart Part Response DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MultipartPartResponse {

    /**
     * Upload ID
     */
    private String uploadId;

    /**
     * Part number (1-based)
     */
    private Integer partNumber;

    /**
     * ETag returned by MinIO for this part
     */
    private String etag;

    /**
     * Size of this part in bytes
     */
    private Long partSize;

    /**
     * Upload status
     */
    private String status;
}
