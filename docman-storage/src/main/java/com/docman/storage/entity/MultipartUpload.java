package com.docman.storage.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Multipart Upload Entity
 * Tracks ongoing multipart uploads
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("multipart_upload")
public class MultipartUpload {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * Multipart upload ID from MinIO
     */
    private String uploadId;

    /**
     * File ID (UUID)
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
     * Number of parts uploaded
     */
    private Integer uploadedParts;

    /**
     * MIME type
     */
    private String contentType;

    /**
     * Owner user ID
     */
    private String userId;

    /**
     * Bucket name
     */
    private String bucketName;

    /**
     * Object name in MinIO
     */
    private String objectName;

    /**
     * Upload status: INIT, UPLOADING, COMPLETED, CANCELLED, EXPIRED
     */
    private String status;

    /**
     * Flag indicating if record is deleted
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
     * Expiration time
     */
    private LocalDateTime expireTime;
}
