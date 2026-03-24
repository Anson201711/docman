package com.docman.storage.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Storage Record Entity
 * Stores metadata for files stored in MinIO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("storage_record")
public class StorageRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * File unique identifier (UUID)
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
     * MinIO object name (path in bucket)
     */
    private String objectName;

    /**
     * Bucket name
     */
    private String bucketName;

    /**
     * File checksum (MD5)
     */
    private String checksum;

    /**
     * Owner user ID
     */
    private String userId;

    /**
     * Associated document ID (if linked to a document)
     */
    private String documentId;

    /**
     * Storage quota allocated to this file's owner
     */
    private Long quotaAllocated;

    /**
     * Storage quota used by this file's owner
     */
    private Long quotaUsed;

    /**
     * Upload status: PENDING, UPLOADING, COMPLETED, FAILED
     */
    private String status;

    /**
     * Multipart upload ID (if applicable)
     */
    private String uploadId;

    /**
     * Flag indicating if file is deleted
     */
    @TableLogic
    private Integer deleted;

    /**
     * Creation time
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * Last update time
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * Deletion time (if deleted)
     */
    private LocalDateTime deleteTime;
}
