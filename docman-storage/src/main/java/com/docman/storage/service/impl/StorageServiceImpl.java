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
import io.minio.CreateMultipartUploadArgs;
import io.minio.MinioClient;
import io.minio.ObjectWriteResponse;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import io.minio.UploadPartArgs;
import io.minio.ListPartsArgs;
import io.minio.CompleteMultipartUploadArgs;
import io.minio.AbortMultipartUploadArgs;
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

            // Upload to MinIO
            String contentType = file.getContentType();
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(minioConfig.getBucketName())
                            .object(objectName)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(contentType)
                            .build()
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
            throw new RuntimeException("File upload failed: " + e.getMessage(), e);
        }
    }

    // ==================== Multipart Upload ====================

    @Override
    @Transactional
    public MultipartInitResponse initMultipartUpload(String fileName, Long fileSize, String contentType, String userId, String documentId) {
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

            // Generate IDs
            String fileId = UUID.randomUUID().toString();
            String objectName = generateObjectName(fileName);
            String extension = getFileExtension(fileName);

            // Calculate part size (default 5MB, minimum 5MB)
            long partSize = 5 * 1024 * 1024; // 5MB
            int totalParts = (int) Math.ceil((double) fileSize / partSize);

            // Initialize multipart upload on MinIO
            io.minio.messages.Upload multipartUpload = minioClient.createMultipartUpload(
                    CreateMultipartUploadArgs.builder()
                            .bucket(minioConfig.getBucketName())
                            .object(objectName)
                            .contentType(contentType != null ? contentType : "application/octet-stream")
                            .build()
            );

            String uploadId = multipartUpload.uploadId();

            // Create multipart upload record
            MultipartUpload upload = MultipartUpload.builder()
                    .uploadId(uploadId)
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
                    .uploadId(uploadId)
                    .status(STATUS_PENDING)
                    .build();

            storageRecordMapper.insert(record);

            return MultipartInitResponse.builder()
                    .uploadId(uploadId)
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

            // Upload part to MinIO
            ByteArrayInputStream bais = new ByteArrayInputStream(data);
            ObjectWriteResponse response = minioClient.uploadPart(
                    UploadPartArgs.builder()
                            .bucket(upload.getBucketName())
                            .object(upload.getObjectName())
                            .uploadId(uploadId)
                            .partNumber(partNumber)
                            .stream(bais, data.length)
                            .length((long) data.length)
                            .build()
            );

            String etag = response.etag();

            // Update uploaded parts count
            upload.setUploadedParts(upload.getUploadedParts() + 1);
            multipartUploadMapper.updateById(upload);

            return MultipartPartResponse.builder()
                    .uploadId(uploadId)
                    .partNumber(partNumber)
                    .etag(etag)
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

            if (!STATUS_UPLOADING.equals(upload.getStatus())) {
                throw new RuntimeException("Multipart upload is not in UPLOADING state");
            }

            // List uploaded parts
            List<Part> parts = minioClient.listParts(
                    ListPartsArgs.builder()
                            .bucket(upload.getBucketName())
                            .object(upload.getObjectName())
                            .uploadId(uploadId)
                            .build()
            );

            // Sort parts by part number
            parts.sort(Comparator.comparingInt(p -> p.partNumber()));

            // Complete multipart upload
            minioClient.completeMultipartUpload(
                    CompleteMultipartUploadArgs.builder()
                            .bucket(upload.getBucketName())
                            .object(upload.getObjectName())
                            .uploadId(uploadId)
                            .parts(parts)
                            .build()
            );

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

            // Build part etag list
            List<MultipartCompleteResponse.PartEtag> partEtags = parts.stream()
                    .map(p -> MultipartCompleteResponse.PartEtag.builder()
                            .partNumber(p.partNumber())
                            .etag(p.etag())
                            .build())
                    .collect(Collectors.toList());

            return MultipartCompleteResponse.builder()
                    .fileId(upload.getFileId())
                    .fileName(upload.getFileName())
                    .extension(upload.getExtension())
                    .contentType(upload.getContentType())
                    .fileSize(upload.getTotalSize())
                    .partsUploaded(parts.size())
                    .objectName(upload.getObjectName())
                    .etag(stat.etag())
                    .downloadUrl(downloadUrl)
                    .status(STATUS_COMPLETED)
                    .partEtags(partEtags)
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

            // Abort multipart upload on MinIO
            minioClient.abortMultipartUpload(
                    AbortMultipartUploadArgs.builder()
                            .bucket(upload.getBucketName())
                            .object(upload.getObjectName())
                            .uploadId(uploadId)
                            .build()
            );

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

    @Override
    public StorageRecord getFileInfo(String fileId, String userId) {
        return findStorageRecord(fileId, userId);
    }

    // ==================== Presigned URL ====================

    @Override
    public PresignedUrlResponse generatePresignedUrl(String fileId, Integer expiresIn, String userId) {
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
                    .fileId(null)
                    .fileName(objectName)
                    .presignedUrl(presignedUrl)
                    .expiresIn(expiry)
                    .expiresAt(expiresAt.format(formatter))
                    .build();

        } catch (Exception e) {
            log.error("Failed to generate upload presigned URL: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate upload presigned URL: " + e.getMessage(), e);
        }
    }

    // ==================== File Delete ====================

    @Override
    @Transactional
    public void deleteFile(String fileId, String userId) {
        StorageRecord record = findStorageRecord(fileId, userId);
        deleteFileByObjectName(record.getObjectName(), userId);
    }

    @Override
    @Transactional
    public void deleteFileByObjectName(String objectName, String userId) {
        try {
            // Delete from MinIO
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(minioConfig.getBucketName())
                            .object(objectName)
                            .build()
            );

            // Find and update storage record
            LambdaQueryWrapper<StorageRecord> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(StorageRecord::getObjectName, objectName)
                    .eq(StorageRecord::getUserId, userId);
            StorageRecord record = storageRecordMapper.selectOne(queryWrapper);

            if (record != null) {
                // Update quota usage (decrease)
                updateQuotaUsage(userId, record.getFileSize(), false);

                // Soft delete record
                storageRecordMapper.delete(queryWrapper);
            }

            log.info("File deleted: objectName={}, userId={}", objectName, userId);

        } catch (Exception e) {
            log.error("Failed to delete file: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to delete file: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void batchDeleteFiles(List<String> fileIds, String userId) {
        for (String fileId : fileIds) {
            deleteFile(fileId, userId);
        }
    }

    // ==================== Storage Quota ====================

    @Override
    public StorageQuotaResponse getStorageQuota(String userId) {
        StorageQuota quota = findOrCreateStorageQuota(userId);

        Long maxQuota = quota.getMaxQuota();
        Long usedQuota = quota.getUsedQuota();
        Long availableQuota = maxQuota - usedQuota;
        Double usagePercentage = maxQuota > 0 ? (usedQuota * 100.0 / maxQuota) : 0.0;

        return StorageQuotaResponse.builder()
                .userId(userId)
                .maxQuota(maxQuota)
                .usedQuota(usedQuota)
                .availableQuota(availableQuota)
                .usagePercentage(usagePercentage)
                .active(quota.getActive() == 1)
                .build();
    }

    @Override
    @Transactional
    public void updateStorageQuota(String userId, Long maxQuota, String operatorId) {
        StorageQuota quota = findOrCreateStorageQuota(userId);
        quota.setMaxQuota(maxQuota);
        storageQuotaMapper.updateById(quota);

        log.info("Storage quota updated: userId={}, maxQuota={}, operator={}", userId, maxQuota, operatorId);
    }

    @Override
    public boolean checkQuota(String userId, Long fileSize) {
        StorageQuota quota = findOrCreateStorageQuota(userId);
        return (quota.getUsedQuota() + fileSize) <= quota.getMaxQuota();
    }

    @Override
    @Transactional
    public void recalculateUsedQuota(String userId) {
        // Calculate total used quota from all completed files
        LambdaQueryWrapper<StorageRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(StorageRecord::getUserId, userId)
                .eq(StorageRecord::getStatus, STATUS_COMPLETED);
        List<StorageRecord> records = storageRecordMapper.selectList(queryWrapper);

        long totalUsed = records.stream()
                .mapToLong(StorageRecord::getFileSize)
                .sum();

        StorageQuota quota = findOrCreateStorageQuota(userId);
        quota.setUsedQuota(totalUsed);
        storageQuotaMapper.updateById(quota);

        log.info("Recalculated used quota: userId={}, usedQuota={}", userId, totalUsed);
    }

    // ==================== Bucket Operations ====================

    @Override
    public void ensureBucketExists(String bucketName) {
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

    @Override
    public List<StorageRecord> listFiles(String prefix, String userId) {
        try {
            // This would require MinIO listObjects with prefix
            // For now, query from database
            LambdaQueryWrapper<StorageRecord> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(StorageRecord::getUserId, userId)
                    .eq(StorageRecord::getStatus, STATUS_COMPLETED);
            if (prefix != null && !prefix.isEmpty()) {
                queryWrapper.likeRight(StorageRecord::getObjectName, prefix);
            }
            return storageRecordMapper.selectList(queryWrapper);
        } catch (Exception e) {
            log.error("Failed to list files: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to list files: " + e.getMessage(), e);
        }
    }

    // ==================== Helper Methods ====================

    private StorageRecord findStorageRecord(String fileId, String userId) {
        LambdaQueryWrapper<StorageRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(StorageRecord::getFileId, fileId)
                .eq(StorageRecord::getUserId, userId);
        StorageRecord record = storageRecordMapper.selectOne(queryWrapper);
        if (record == null) {
            throw new RuntimeException("File not found: " + fileId);
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

    private StorageQuota findOrCreateStorageQuota(String userId) {
        LambdaQueryWrapper<StorageQuota> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(StorageQuota::getUserId, userId);
        StorageQuota quota = storageQuotaMapper.selectOne(queryWrapper);

        if (quota == null) {
            quota = StorageQuota.builder()
                    .userId(userId)
                    .maxQuota(storageConfig.getDefaultQuota())
                    .usedQuota(0L)
                    .active(1)
                    .build();
            storageQuotaMapper.insert(quota);
        }

        return quota;
    }

    private void updateQuotaUsage(String userId, Long fileSize, boolean increase) {
        StorageQuota quota = findOrCreateStorageQuota(userId);
        if (increase) {
            quota.setUsedQuota(quota.getUsedQuota() + fileSize);
        } else {
            quota.setUsedQuota(Math.max(0, quota.getUsedQuota() - fileSize));
        }
        storageQuotaMapper.updateById(quota);
    }

    private String generateObjectName(String fileName) {
        String extension = getFileExtension(fileName);
        String uuid = UUID.randomUUID().toString().replace("-", "");
        String timestamp = String.valueOf(System.currentTimeMillis());
        return String.format("files/%s/%s.%s",
                timestamp.substring(0, 8),
                uuid,
                extension);
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

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("File is empty");
        }

        // Check file size
        if (storageConfig.getMaxFileSize() != null && file.getSize() > storageConfig.getMaxFileSize()) {
            throw new RuntimeException("File size exceeds maximum allowed size");
        }

        // Check file extension
        String extension = getFileExtension(file.getOriginalFilename());
        if (storageConfig.getAllowedExtensions() != null &&
                !storageConfig.getAllowedExtensions().isEmpty() &&
                !storageConfig.getAllowedExtensions().contains(extension.toLowerCase())) {
            throw new RuntimeException("File type not allowed: " + extension);
        }
    }

    private boolean isValidExtension(String extension) {
        if (storageConfig.getAllowedExtensions() == null || storageConfig.getAllowedExtensions().isEmpty()) {
            return true;
        }
        return storageConfig.getAllowedExtensions().contains(extension.toLowerCase());
    }
}
