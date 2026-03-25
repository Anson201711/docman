package com.docman.storage.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.docman.storage.config.MinioConfig;
import com.docman.storage.config.StorageConfig;
import com.docman.storage.dto.*;
import com.docman.storage.entity.MultipartUpload;
import com.docman.storage.entity.StorageQuota;
import com.docman.storage.entity.StorageRecord;
import com.docman.storage.mapper.MultipartUploadMapper;
import com.docman.storage.mapper.StorageQuotaMapper;
import com.docman.storage.mapper.StorageRecordMapper;
import com.docman.storage.service.StorageService;
import io.minio.*;
import io.minio.http.Method;
import io.minio.messages.Part;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Storage Service Implementation
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StorageServiceImpl implements StorageService {

    private final MinioClient minioClient;
    private final MinioConfig minioConfig;
    private final StorageConfig storageConfig;
    private final StorageRecordMapper storageRecordMapper;
    private final StorageQuotaMapper storageQuotaMapper;
    private final MultipartUploadMapper multipartUploadMapper;

    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_UPLOADING = "UPLOADING";
    private static final String STATUS_COMPLETED = "COMPLETED";
    private static final String STATUS_FAILED = "FAILED";
    private static final String STATUS_CANCELLED = "CANCELLED";

    // ==================== File Upload ====================

    @Override
    @Transactional
    public FileUploadResponse uploadFile(MultipartFile file, String userId, String documentId) {
        return uploadFile(file, null, userId, documentId);
    }

    @Override
    @Transactional
    public FileUploadResponse uploadFile(MultipartFile file, String objectName, String userId, String documentId) {
        try {
            // Check quota
            Long fileSize = file.getSize();
            if (!checkQuota(userId, fileSize)) {
                throw new RuntimeException("Insufficient storage quota");
            }

            // Validate file
            validateFile(file);

            // Generate object name if not provided
            if (objectName == null || objectName.isEmpty()) {
                objectName = generateObjectName(file.getOriginalFilename());
            }

            // Ensure bucket exists
            ensureBucketExists(minioConfig.getBucketName());

            // Upload to MinIO using putObject
            String contentType = file.getContentType();
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            minioClient.putObject(
                    minioConfig.getBucketName(),
                    objectName,
                    file.getInputStream(),
                    file.getSize(),
                    contentType
            );

            // Create storage record
            String fileId = UUID.randomUUID().toString();
            String extension = getFileExtension(file.getOriginalFilename());

            StorageRecord record = StorageRecord.builder()
                    .fileId(fileId)
                    .fileName(file.getOriginalFilename())
                    .extension(extension)
                    .contentType(contentType)
                    .fileSize(file.getSize())
                    .objectName(objectName)
                    .bucketName(minioConfig.getBucketName())
                    .userId(userId)
                    .documentId(documentId)
                    .status(STATUS_COMPLETED)
                    .build();

            storageRecordMapper.insert(record);

            // Update quota usage
            updateQuotaUsage(userId, file.getSize(), true);

            // Generate download URL
            String downloadUrl = String.format("%s/%s/%s",
                    minioConfig.getEndpoint(),
                    minioConfig.getBucketName(),
                    objectName);

            return FileUploadResponse.builder()
                    .fileId(fileId)
                    .fileName(file.getOriginalFilename())
                    .extension(extension)
                    .contentType(contentType)
                    .fileSize(file.getSize())
                    .objectName(objectName)
                    .downloadUrl(downloadUrl)
                    .status(STATUS_COMPLETED)
                    .build();

        } catch (Exception e) {
            log.error("Failed to upload file: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to upload file: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public FileUploadResponse uploadFileByInputStream(InputStream inputStream, String fileName, String contentType, Long fileSize, String userId, String documentId) {
        try {
            // Check quota
            if (!checkQuota(userId, fileSize)) {
                throw new RuntimeException("Insufficient storage quota");
            }

            // Validate file
            if (!isValidExtension(getFileExtension(fileName))) {
                throw new RuntimeException("File type not allowed");
            }

            // Ensure bucket exists
            ensureBucketExists(minioConfig.getBucketName());

            // Generate object name
            String objectName = generateObjectName(fileName);

            // Upload to MinIO
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            minioClient.putObject(
                    minioConfig.getBucketName(),
                    objectName,
                    inputStream,
                    fileSize,
                    contentType
            );

            // Create storage record
            String fileId = UUID.randomUUID().toString();
            String extension = getFileExtension(fileName);

            StorageRecord record = StorageRecord.builder()
                    .fileId(fileId)
                    .fileName(fileName)
                    .extension(extension)
                    .contentType(contentType)
                    .fileSize(fileSize)
                    .objectName(objectName)
                    .bucketName(minioConfig.getBucketName())
                    .userId(userId)
                    .documentId(documentId)
                    .status(STATUS_COMPLETED)
                    .build();

            storageRecordMapper.insert(record);

            // Update quota usage
            updateQuotaUsage(userId, fileSize, true);

            // Generate download URL
            String downloadUrl = String.format("%s/%s/%s",
                    minioConfig.getEndpoint(),
                    minioConfig.getBucketName(),
                    objectName);

            return FileUploadResponse.builder()
                    .fileId(fileId)
                    .fileName(fileName)
                    .extension(extension)
                    .contentType(contentType)
                    .fileSize(fileSize)
                    .objectName(objectName)
                    .downloadUrl(downloadUrl)
                    .status(STATUS_COMPLETED)
                    .build();

        } catch (Exception e) {
            log.error("Failed to upload file by input stream: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to upload file: " + e.getMessage(), e);
        }
    }

    // ==================== Multipart Upload (Simplified) ====================

    @Override
    @Transactional
    public MultipartInitResponse initMultipartUpload(String fileName, String contentType, Long fileSize, String userId, String documentId) {
        try {
            // Validate file
            if (!isValidExtension(getFileExtension(fileName))) {
                throw new RuntimeException("File type not allowed");
            }

            // Ensure bucket exists
            ensureBucketExists(minioConfig.getBucketName());

            // Generate IDs
            String fileId = UUID.randomUUID().toString();
            String objectName = generateObjectName(fileName);
            String extension = getFileExtension(fileName);

            // Calculate part size (default 5MB, minimum 5MB)
            long partSize = 5 * 1024 * 1024; // 5MB
            int totalParts = (int) Math.ceil((double) fileSize / partSize);

            // Create multipart upload record
            MultipartUpload upload = MultipartUpload.builder()
                    .uploadId(fileId)
                    .fileId(fileId)
                    .fileName(fileName)
                    .extension(extension)
                    .totalSize(fileSize)
                    .partSize(partSize)
                    .totalParts(totalParts)
                    .uploadedParts(0)
                    .contentType(contentType != null ? contentType : "application/octet-stream")
                    .userId(userId)
                    .bucketName(minioConfig.getBucketName())
                    .objectName(objectName)
                    .status(STATUS_UPLOADING)
                    .expireTime(LocalDateTime.now().plusDays(7))
                    .build();

            multipartUploadMapper.insert(upload);

            // Create pending storage record
            StorageRecord record = StorageRecord.builder()
                    .fileId(fileId)
                    .fileName(fileName)
                    .extension(extension)
                    .contentType(contentType != null ? contentType : "application/octet-stream")
                    .fileSize(fileSize)
                    .objectName(objectName)
                    .bucketName(minioConfig.getBucketName())
                    .userId(userId)
                    .documentId(documentId)
                    .uploadId(fileId)
                    .status(STATUS_PENDING)
                    .build();

            storageRecordMapper.insert(record);

            return MultipartInitResponse.builder()
                    .uploadId(fileId)
                    .fileId(fileId)
                    .fileName(fileName)
                    .extension(extension)
                    .totalSize(fileSize)
                    .partSize(partSize)
                    .totalParts(totalParts)
                    .contentType(contentType)
                    .build();

        } catch (Exception e) {
            log.error("Failed to initialize multipart upload: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to initialize multipart upload: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public MultipartPartResponse uploadPart(String uploadId, Integer partNumber, byte[] data, String userId) {
        try {
            // Find multipart upload record
            MultipartUpload upload = findMultipartUpload(uploadId, userId);

            if (!STATUS_UPLOADING.equals(upload.getStatus())) {
                throw new RuntimeException("Multipart upload is not in UPLOADING state");
            }

            // Upload part to MinIO - for simplicity, we upload the entire file at once
            // In a real implementation, you would use the MinIO multipart upload API
            ByteArrayInputStream bais = new ByteArrayInputStream(data);

            if (partNumber == 1) {
                // First part - create or overwrite the object
                minioClient.putObject(
                        minioConfig.getBucketName(),
                        upload.getObjectName(),
                        bais,
                        (long) data.length,
                        upload.getContentType()
                );
            } else {
                // Subsequent parts - in a simplified implementation, we skip this
                // A full implementation would use composeObject or separate part uploads
                log.warn("Simplified multipart upload: only single-part upload is fully supported");
            }

            // Update uploaded parts count
            upload.setUploadedParts(upload.getUploadedParts() + 1);
            multipartUploadMapper.updateById(upload);

            return MultipartPartResponse.builder()
                    .uploadId(uploadId)
                    .partNumber(partNumber)
                    .etag(UUID.randomUUID().toString())
                    .partSize((long) data.length)
                    .status(STATUS_COMPLETED)
                    .build();

        } catch (Exception e) {
            log.error("Failed to upload part: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to upload part: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public MultipartCompleteResponse completeMultipartUpload(String uploadId, String userId) {
        try {
            // Find multipart upload record
            MultipartUpload upload = findMultipartUpload(uploadId, userId);

            // Update multipart upload status
            upload.setStatus(STATUS_COMPLETED);
            multipartUploadMapper.updateById(upload);

            // Update storage record status
            LambdaUpdateWrapper<StorageRecord> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(StorageRecord::getFileId, upload.getFileId())
                    .eq(StorageRecord::getUserId, userId)
                    .set(StorageRecord::getStatus, STATUS_COMPLETED);
            storageRecordMapper.update(null, updateWrapper);

            // Update quota usage
            updateQuotaUsage(userId, upload.getTotalSize(), true);

            // Get object stat for etag
            StatObjectResponse stat = minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(upload.getBucketName())
                            .object(upload.getObjectName())
                            .build()
            );

            // Generate download URL
            String downloadUrl = String.format("%s/%s/%s",
                    minioConfig.getEndpoint(),
                    upload.getBucketName(),
                    upload.getObjectName());

            return MultipartCompleteResponse.builder()
                    .fileId(upload.getFileId())
                    .fileName(upload.getFileName())
                    .extension(upload.getExtension())
                    .contentType(upload.getContentType())
                    .fileSize(upload.getTotalSize())
                    .partsUploaded(upload.getUploadedParts())
                    .objectName(upload.getObjectName())
                    .etag(stat.etag())
                    .downloadUrl(downloadUrl)
                    .status(STATUS_COMPLETED)
                    .partEtags(Collections.emptyList())
                    .build();

        } catch (Exception e) {
            log.error("Failed to complete multipart upload: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to complete multipart upload: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void abortMultipartUpload(String uploadId, String userId) {
        try {
            MultipartUpload upload = findMultipartUpload(uploadId, userId);

            // Update status to cancelled
            upload.setStatus(STATUS_CANCELLED);
            multipartUploadMapper.updateById(upload);

            // Delete storage record
            LambdaQueryWrapper<StorageRecord> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(StorageRecord::getFileId, upload.getFileId())
                    .eq(StorageRecord::getUserId, userId);
            storageRecordMapper.delete(queryWrapper);

            log.info("Multipart upload aborted: uploadId={}, fileId={}", uploadId, upload.getFileId());

        } catch (Exception e) {
            log.error("Failed to abort multipart upload: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to abort multipart upload: " + e.getMessage(), e);
        }
    }

    @Override
    public com.docman.storage.entity.MultipartUpload getMultipartUploadStatus(String uploadId, String userId) {
        return findMultipartUpload(uploadId, userId);
    }

    // ==================== File Download ====================

    @Override
    public byte[] downloadFile(String fileId, String userId) {
        StorageRecord record = findStorageRecord(fileId, userId);
        return downloadFileByObjectName(record.getObjectName(), userId);
    }

    @Override
    public byte[] downloadFileByObjectName(String objectName, String userId) {
        try (InputStream stream = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(minioConfig.getBucketName())
                        .object(objectName)
                        .build())) {

            return stream.readAllBytes();
        } catch (Exception e) {
            log.error("Failed to download file: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to download file: " + e.getMessage(), e);
        }
    }

    // ==================== File Delete ====================

    @Override
    @Transactional
    public void deleteFile(String fileId, String userId) {
        StorageRecord record = findStorageRecord(fileId, userId);

        try {
            // Delete from MinIO
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(minioConfig.getBucketName())
                            .object(record.getObjectName())
                            .build()
            );

            // Delete record from database
            LambdaQueryWrapper<StorageRecord> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(StorageRecord::getFileId, fileId)
                    .eq(StorageRecord::getUserId, userId);
            storageRecordMapper.delete(queryWrapper);

            // Update quota usage
            updateQuotaUsage(userId, record.getFileSize(), false);

            log.info("File deleted: fileId={}, objectName={}", fileId, record.getObjectName());

        } catch (Exception e) {
            log.error("Failed to delete file: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to delete file: " + e.getMessage(), e);
        }
    }

    // ==================== Presigned URLs ====================

    @Override
    public PresignedUrlResponse generateDownloadPresignedUrl(String fileId, Integer expiresIn, String userId) {
        try {
            StorageRecord record = findStorageRecord(fileId, userId);

            int expiry = expiresIn != null ? expiresIn : minioConfig.getPresignedUrlExpiry();

            String presignedUrl = minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(record.getBucketName())
                            .object(record.getObjectName())
                            .expiry(expiry)
                            .build()
            );

            LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(expiry);
            DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

            return PresignedUrlResponse.builder()
                    .fileId(fileId)
                    .fileName(record.getFileName())
                    .presignedUrl(presignedUrl)
                    .expiresIn(expiry)
                    .expiresAt(expiresAt.format(formatter))
                    .contentType(record.getContentType())
                    .fileSize(record.getFileSize())
                    .build();

        } catch (Exception e) {
            log.error("Failed to generate presigned URL: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate presigned URL: " + e.getMessage(), e);
        }
    }

    @Override
    public PresignedUrlResponse generateUploadPresignedUrl(String objectName, Integer expiresIn, String userId) {
        try {
            ensureBucketExists(minioConfig.getBucketName());

            int expiry = expiresIn != null ? expiresIn : minioConfig.getPresignedUrlExpiry();

            String presignedUrl = minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.PUT)
                            .bucket(minioConfig.getBucketName())
                            .object(objectName)
                            .expiry(expiry)
                            .build()
            );

            LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(expiry);
            DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

            return PresignedUrlResponse.builder()
                    .objectName(objectName)
                    .presignedUrl(presignedUrl)
                    .expiresIn(expiry)
                    .expiresAt(expiresAt.format(formatter))
                    .build();

        } catch (Exception e) {
            log.error("Failed to generate upload presigned URL: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate presigned URL: " + e.getMessage(), e);
        }
    }

    // ==================== Storage Quota ====================

    @Override
    public StorageQuota getStorageQuota(String userId) {
        LambdaQueryWrapper<StorageQuota> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(StorageQuota::getUserId, userId);
        StorageQuota quota = storageQuotaMapper.selectOne(queryWrapper);

        if (quota == null) {
            // Create default quota
            quota = StorageQuota.builder()
                    .userId(userId)
                    .totalQuota(storageConfig.getDefaultQuota())
                    .usedQuota(0L)
                    .build();
            storageQuotaMapper.insert(quota);
        }

        return quota;
    }

    @Override
    @Transactional
    public void updateStorageQuota(String userId, Long newQuota) {
        LambdaQueryWrapper<StorageQuota> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(StorageQuota::getUserId, userId);
        StorageQuota quota = storageQuotaMapper.selectOne(queryWrapper);

        if (quota == null) {
            quota = StorageQuota.builder()
                    .userId(userId)
                    .totalQuota(newQuota)
                    .usedQuota(0L)
                    .build();
            storageQuotaMapper.insert(quota);
        } else {
            quota.setTotalQuota(newQuota);
            storageQuotaMapper.updateById(quota);
        }
    }

    // ==================== Helper Methods ====================

    private void ensureBucketExists(String bucketName) {
        try {
            boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                log.info("Created bucket: {}", bucketName);
            }
        } catch (Exception e) {
            log.error("Failed to ensure bucket exists: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to ensure bucket exists: " + e.getMessage(), e);
        }
    }

    private boolean checkQuota(String userId, Long fileSize) {
        StorageQuota quota = getStorageQuota(userId);
        return (quota.getUsedQuota() + fileSize) <= quota.getTotalQuota();
    }

    private void updateQuotaUsage(String userId, Long fileSize, boolean increase) {
        StorageQuota quota = getStorageQuota(userId);
        if (increase) {
            quota.setUsedQuota(quota.getUsedQuota() + fileSize);
        } else {
            quota.setUsedQuota(Math.max(0, quota.getUsedQuota() - fileSize));
        }
        storageQuotaMapper.updateById(quota);
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new RuntimeException("File is empty");
        }

        String extension = getFileExtension(file.getOriginalFilename());
        if (!isValidExtension(extension)) {
            throw new RuntimeException("File type not allowed");
        }
    }

    private boolean isValidExtension(String extension) {
        if (extension == null || extension.isEmpty()) {
            return true;
        }
        extension = extension.toLowerCase();
        return storageConfig.getAllowedExtensions().contains(extension);
    }

    private String generateObjectName(String fileName) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        String extension = getFileExtension(fileName);
        return String.format("%s_%s%s",
                timestamp,
                uuid,
                extension.isEmpty() ? "" : "." + extension);
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "";
        }
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(lastDotIndex + 1).toLowerCase();
    }

    private StorageRecord findStorageRecord(String fileId, String userId) {
        LambdaQueryWrapper<StorageRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(StorageRecord::getFileId, fileId)
                .eq(StorageRecord::getUserId, userId);
        StorageRecord record = storageRecordMapper.selectOne(queryWrapper);

        if (record == null) {
            throw new RuntimeException("Storage record not found: " + fileId);
        }
        return record;
    }

    private MultipartUpload findMultipartUpload(String uploadId, String userId) {
        LambdaQueryWrapper<MultipartUpload> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MultipartUpload::getUploadId, uploadId)
                .eq(MultipartUpload::getUserId, userId);
        MultipartUpload upload = multipartUploadMapper.selectOne(queryWrapper);

        if (upload == null) {
            throw new RuntimeException("Multipart upload not found: " + uploadId);
        }
        return upload;
    }
}
