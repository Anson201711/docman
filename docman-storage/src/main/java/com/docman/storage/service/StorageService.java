package com.docman.storage.service;

import com.docman.storage.dto.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Storage Service Interface
 */
public interface StorageService {

    // ==================== File Upload ====================

    /**
     * Upload a single file
     *
     * @param file Multipart file to upload
     * @param userId User ID
     * @param documentId Document ID (optional)
     * @return FileUploadResponse
     */
    FileUploadResponse uploadFile(MultipartFile file, String userId, String documentId);

    /**
     * Upload a file with specified object name
     *
     * @param file Multipart file to upload
     * @param objectName Object name in MinIO
     * @param userId User ID
     * @param documentId Document ID (optional)
     * @return FileUploadResponse
     */
    FileUploadResponse uploadFile(MultipartFile file, String objectName, String userId, String documentId);

    // ==================== Multipart Upload ====================

    /**
     * Initialize multipart upload
     *
     * @param fileName Original filename
     * @param fileSize Total file size in bytes
     * @param contentType MIME type
     * @param userId User ID
     * @param documentId Document ID (optional)
     * @return MultipartInitResponse
     */
    MultipartInitResponse initMultipartUpload(String fileName, Long fileSize, String contentType, String userId, String documentId);

    /**
     * Upload a single part of multipart upload
     *
     * @param uploadId Upload ID from initialization
     * @param partNumber Part number (1-based)
     * @param data Part data
     * @param userId User ID
     * @return MultipartPartResponse
     */
    MultipartPartResponse uploadPart(String uploadId, Integer partNumber, byte[] data, String userId);

    /**
     * Complete multipart upload
     *
     * @param uploadId Upload ID
     * @param userId User ID
     * @return MultipartCompleteResponse
     */
    MultipartCompleteResponse completeMultipartUpload(String uploadId, String userId);

    /**
     * Cancel multipart upload
     *
     * @param uploadId Upload ID
     * @param userId User ID
     */
    void abortMultipartUpload(String uploadId, String userId);

    /**
     * Get multipart upload status
     *
     * @param uploadId Upload ID
     * @param userId User ID
     * @return MultipartUpload details
     */
    com.docman.storage.entity.MultipartUpload getMultipartUploadStatus(String uploadId, String userId);

    // ==================== File Download ====================

    /**
     * Download file by file ID
     *
     * @param fileId File ID
     * @param userId User ID
     * @return byte[] File content
     */
    byte[] downloadFile(String fileId, String userId);

    /**
     * Download file by object name
     *
     * @param objectName Object name in MinIO
     * @param userId User ID
     * @return byte[] File content
     */
    byte[] downloadFileByObjectName(String objectName, String userId);

    /**
     * Get file info by file ID
     *
     * @param fileId File ID
     * @param userId User ID
     * @return StorageRecord
     */
    com.docman.storage.entity.StorageRecord getFileInfo(String fileId, String userId);

    // ==================== Presigned URL ====================

    /**
     * Generate presigned URL for file download
     *
     * @param fileId File ID
     * @param expiresIn Expiration time in seconds
     * @param userId User ID
     * @return PresignedUrlResponse
     */
    PresignedUrlResponse generatePresignedUrl(String fileId, Integer expiresIn, String userId);

    /**
     * Generate presigned URL for file upload
     *
     * @param objectName Object name
     * @param expiresIn Expiration time in seconds
     * @param userId User ID
     * @return PresignedUrlResponse
     */
    PresignedUrlResponse generateUploadPresignedUrl(String objectName, Integer expiresIn, String userId);

    // ==================== File Delete ====================

    /**
     * Delete file by file ID
     *
     * @param fileId File ID
     * @param userId User ID
     */
    void deleteFile(String fileId, String userId);

    /**
     * Delete file by object name
     *
     * @param objectName Object name in MinIO
     * @param userId User ID
     */
    void deleteFileByObjectName(String objectName, String userId);

    /**
     * Batch delete files
     *
     * @param fileIds List of file IDs
     * @param userId User ID
     */
    void batchDeleteFiles(List<String> fileIds, String userId);

    // ==================== Storage Quota ====================

    /**
     * Get storage quota for user
     *
     * @param userId User ID
     * @return StorageQuotaResponse
     */
    StorageQuotaResponse getStorageQuota(String userId);

    /**
     * Update storage quota for user
     *
     * @param userId User ID
     * @param maxQuota New max quota in bytes
     * @param operatorId Operator user ID
     */
    void updateStorageQuota(String userId, Long maxQuota, String operatorId);

    /**
     * Check if user has enough quota for file size
     *
     * @param userId User ID
     * @param fileSize File size to check
     * @return true if enough quota available
     */
    boolean checkQuota(String userId, Long fileSize);

    /**
     * Recalculate user's used quota
     *
     * @param userId User ID
     */
    void recalculateUsedQuota(String userId);

    // ==================== Bucket Operations ====================

    /**
     * Ensure bucket exists
     *
     * @param bucketName Bucket name
     */
    void ensureBucketExists(String bucketName);

    /**
     * List files in bucket
     *
     * @param prefix Prefix filter
     * @param userId User ID
     * @return List of storage records
     */
    List<com.docman.storage.entity.StorageRecord> listFiles(String prefix, String userId);
}
