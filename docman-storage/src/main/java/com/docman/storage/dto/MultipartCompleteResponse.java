package com.docman.storage.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Multipart Complete Response DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MultipartCompleteResponse {

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
     * Total file size in bytes
     */
    private Long fileSize;

    /**
     * Number of parts uploaded
     */
    private Integer partsUploaded;

    /**
     * MinIO object name
     */
    private String objectName;

    /**
     * ETag of the completed object
     */
    private String etag;

    /**
     * Download URL
     */
    private String downloadUrl;

    /**
     * Upload status
     */
    private String status;

    /**
     * List of uploaded part ETags
     */
    private List<PartEtag> partEtags;

    /**
     * Part ETag info
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PartEtag {
        private Integer partNumber;
        private String etag;
    }
}
