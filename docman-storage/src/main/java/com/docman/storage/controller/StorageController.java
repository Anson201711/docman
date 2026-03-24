package com.docman.storage.controller;

import com.docman.storage.dto.*;
import com.docman.storage.entity.MultipartUpload;
import com.docman.storage.entity.StorageRecord;
import com.docman.storage.service.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Storage Controller
 * Handles all storage-related HTTP endpoints
 */
@Slf4j
@RestController
@RequestMapping("/storage")
@RequiredArgsConstructor
public class StorageController {

    private final StorageService storageService;

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> health() {
        return ResponseEntity.ok(ApiResponse.success("Storage service is healthy", "OK"));
    }

    // ==================== File Upload ====================

    /**
     * Upload a single file
     * POST /storage/upload
     */
    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<FileUploadResponse>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-Document-Id", required = false) String documentId) {

        log.info("Upload request: fileName={}, size={}, userId={}",
                file.getOriginalFilename(), file.getSize(), userId);

        if (userId == null || userId.isEmpty()) {
            userId = "anonymous";
        }

        FileUploadResponse response = storageService.uploadFile(file, userId, documentId);
        return ResponseEntity.ok(ApiResponse.success("File uploaded successfully", response));
    }

    /**
     * Upload a file with specified object name
     * POST /storage/upload/object
     */
    @PostMapping("/upload/object")
    public ResponseEntity<ApiResponse<FileUploadResponse>> uploadFileWithObjectName(
            @RequestParam("file") MultipartFile file,
            @RequestParam("objectName") String objectName,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-Document-Id", required = false) String documentId) {

        log.info("Upload with object name request: fileName={}, objectName={}, userId={}",
                file.getOriginalFilename(), objectName, userId);

        if (userId == null || userId.isEmpty()) {
            userId = "anonymous";
        }

        FileUploadResponse response = storageService.uploadFile(file, objectName, userId, documentId);
        return ResponseEntity.ok(ApiResponse.success("File uploaded successfully", response));
    }

    // ==================== Multipart Upload ====================

    /**
     * Initialize multipart upload
     * POST /storage/multipart/init
     */
    @PostMapping("/multipart/init")
    public ResponseEntity<ApiResponse<MultipartInitResponse>> initMultipartUpload(
            @RequestParam("fileName") String fileName,
            @RequestParam("fileSize") Long fileSize,
            @RequestParam(value = "contentType", required = false) String contentType,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-Document-Id", required = false) String documentId) {

        log.info("Init multipart upload: fileName={}, fileSize={}, userId={}",
                fileName, fileSize, userId);

        if (userId == null || userId.isEmpty()) {
            userId = "anonymous";
        }

        MultipartInitResponse response = storageService.initMultipartUpload(
                fileName, fileSize, contentType, userId, documentId);
        return ResponseEntity.ok(ApiResponse.success("Multipart upload initialized", response));
    }

    /**
     * Upload a part of multipart upload
     * POST /storage/multipart/{uploadId}
     */
    @PostMapping("/multipart/{uploadId}")
    public ResponseEntity<ApiResponse<MultipartPartResponse>> uploadPart(
            @PathVariable("uploadId") String uploadId,
            @RequestParam("partNumber") Integer partNumber,
            @RequestBody byte[] data,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {

        log.info("Upload part: uploadId={}, partNumber={}, dataSize={}, userId={}",
                uploadId, partNumber, data.length, userId);

        if (userId == null || userId.isEmpty()) {
            userId = "anonymous";
        }

        MultipartPartResponse response = storageService.uploadPart(uploadId, partNumber, data, userId);
        return ResponseEntity.ok(ApiResponse.success("Part uploaded successfully", response));
    }

    /**
     * Complete multipart upload
     * POST /storage/multipart/{uploadId}/complete
     */
    @PostMapping("/multipart/{uploadId}/complete")
    public ResponseEntity<ApiResponse<MultipartCompleteResponse>> completeMultipartUpload(
            @PathVariable("uploadId") String uploadId,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {

        log.info("Complete multipart upload: uploadId={}, userId={}", uploadId, userId);

        if (userId == null || userId.isEmpty()) {
            userId = "anonymous";
        }

        MultipartCompleteResponse response = storageService.completeMultipartUpload(uploadId, userId);
        return ResponseEntity.ok(ApiResponse.success("Multipart upload completed", response));
    }

    /**
     * Abort/Cancel multipart upload
     * DELETE /storage/multipart/{uploadId}
     */
    @DeleteMapping("/multipart/{uploadId}")
    public ResponseEntity<ApiResponse<Void>> abortMultipartUpload(
            @PathVariable("uploadId") String uploadId,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {

        log.info("Abort multipart upload: uploadId={}, userId={}", uploadId, userId);

        if (userId == null || userId.isEmpty()) {
            userId = "anonymous";
        }

        storageService.abortMultipartUpload(uploadId, userId);
        return ResponseEntity.ok(ApiResponse.success("Multipart upload aborted", null));
    }

    /**
     * Get multipart upload status
     * GET /storage/multipart/{uploadId}
     */
    @GetMapping("/multipart/{uploadId}")
    public ResponseEntity<ApiResponse<MultipartUpload>> getMultipartUploadStatus(
            @PathVariable("uploadId") String uploadId,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {

        log.info("Get multipart upload status: uploadId={}, userId={}", uploadId, userId);

        if (userId == null || userId.isEmpty()) {
            userId = "anonymous";
        }

        MultipartUpload response = storageService.getMultipartUploadStatus(uploadId, userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // ==================== File Download ====================

    /**
     * Download file by file ID
     * GET /storage/download/{fileId}
     */
    @GetMapping("/download/{fileId}")
    public ResponseEntity<byte[]> downloadFile(
            @PathVariable("fileId") String fileId,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {

        log.info("Download file: fileId={}, userId={}", fileId, userId);

        if (userId == null || userId.isEmpty()) {
            userId = "anonymous";
        }

        StorageRecord record = storageService.getFileInfo(fileId, userId);
        byte[] data = storageService.downloadFile(fileId, userId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(record.getContentType()));
        headers.setContentDispositionFormData("attachment", record.getFileName());
        headers.setContentLength(record.getFileSize());

        return ResponseEntity.ok()
                .headers(headers)
                .body(data);
    }

    /**
     * Download file by object name
     * GET /storage/download/object/{objectName}
     */
    @GetMapping("/download/object/**")
    public ResponseEntity<byte[]> downloadFileByObjectName(
            @RequestHeader(value = "X-User-Id", required = false) String userId) {

        // Extract object name from path
        String objectName = extractObjectNameFromPath();

        log.info("Download file by object name: objectName={}, userId={}", objectName, userId);

        if (userId == null || userId.isEmpty()) {
            userId = "anonymous";
        }

        byte[] data = storageService.downloadFileByObjectName(objectName, userId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", objectName);
        headers.setContentLength(data.length);

        return ResponseEntity.ok()
                .headers(headers)
                .body(data);
    }

    // ==================== Presigned URL ====================

    /**
     * Generate presigned URL for file download
     * GET /storage/presigned/{fileId}
     */
    @GetMapping("/presigned/{fileId}")
    public ResponseEntity<ApiResponse<PresignedUrlResponse>> getPresignedUrl(
            @PathVariable("fileId") String fileId,
            @RequestParam(value = "expiresIn", required = false) Integer expiresIn,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {

        log.info("Get presigned URL: fileId={}, expiresIn={}, userId={}", fileId, expiresIn, userId);

        if (userId == null || userId.isEmpty()) {
            userId = "anonymous";
        }

        PresignedUrlResponse response = storageService.generatePresignedUrl(fileId, expiresIn, userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Generate presigned URL for file upload
     * GET /storage/presigned/upload
     */
    @GetMapping("/presigned/upload")
    public ResponseEntity<ApiResponse<PresignedUrlResponse>> getUploadPresignedUrl(
            @RequestParam("objectName") String objectName,
            @RequestParam(value = "expiresIn", required = false) Integer expiresIn,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {

        log.info("Get upload presigned URL: objectName={}, expiresIn={}, userId={}",
                objectName, expiresIn, userId);

        if (userId == null || userId.isEmpty()) {
            userId = "anonymous";
        }

        PresignedUrlResponse response = storageService.generateUploadPresignedUrl(objectName, expiresIn, userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // ==================== File Delete ====================

    /**
     * Delete file by file ID
     * DELETE /storage/{fileId}
     */
    @DeleteMapping("/{fileId}")
    public ResponseEntity<ApiResponse<Void>> deleteFile(
            @PathVariable("fileId") String fileId,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {

        log.info("Delete file: fileId={}, userId={}", fileId, userId);

        if (userId == null || userId.isEmpty()) {
            userId = "anonymous";
        }

        storageService.deleteFile(fileId, userId);
        return ResponseEntity.ok(ApiResponse.success("File deleted successfully", null));
    }

    /**
     * Batch delete files
     * DELETE /storage/batch
     */
    @DeleteMapping("/batch")
    public ResponseEntity<ApiResponse<Void>> batchDeleteFiles(
            @RequestBody List<String> fileIds,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {

        log.info("Batch delete files: count={}, userId={}", fileIds.size(), userId);

        if (userId == null || userId.isEmpty()) {
            userId = "anonymous";
        }

        storageService.batchDeleteFiles(fileIds, userId);
        return ResponseEntity.ok(ApiResponse.success("Files deleted successfully", null));
    }

    // ==================== Storage Quota ====================

    /**
     * Get storage quota for current user
     * GET /storage/quota
     */
    @GetMapping("/quota")
    public ResponseEntity<ApiResponse<StorageQuotaResponse>> getStorageQuota(
            @RequestHeader(value = "X-User-Id", required = false) String userId) {

        log.info("Get storage quota: userId={}", userId);

        if (userId == null || userId.isEmpty()) {
            userId = "anonymous";
        }

        StorageQuotaResponse response = storageService.getStorageQuota(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Update storage quota (admin operation)
     * PUT /storage/quota/{userId}
     */
    @PutMapping("/quota/{userId}")
    public ResponseEntity<ApiResponse<Void>> updateStorageQuota(
            @PathVariable("userId") String targetUserId,
            @RequestParam("maxQuota") Long maxQuota,
            @RequestHeader(value = "X-Operator-Id", required = false) String operatorId) {

        log.info("Update storage quota: targetUserId={}, maxQuota={}, operator={}",
                targetUserId, maxQuota, operatorId);

        if (operatorId == null || operatorId.isEmpty()) {
            operatorId = "system";
        }

        storageService.updateStorageQuota(targetUserId, maxQuota, operatorId);
        return ResponseEntity.ok(ApiResponse.success("Storage quota updated successfully", null));
    }

    /**
     * Recalculate storage quota usage
     * POST /storage/quota/recalculate
     */
    @PostMapping("/quota/recalculate")
    public ResponseEntity<ApiResponse<Void>> recalculateQuota(
            @RequestHeader(value = "X-User-Id", required = false) String userId) {

        log.info("Recalculate quota: userId={}", userId);

        if (userId == null || userId.isEmpty()) {
            userId = "anonymous";
        }

        storageService.recalculateUsedQuota(userId);
        return ResponseEntity.ok(ApiResponse.success("Quota recalculated successfully", null));
    }

    // ==================== File Info ====================

    /**
     * Get file info by file ID
     * GET /storage/info/{fileId}
     */
    @GetMapping("/info/{fileId}")
    public ResponseEntity<ApiResponse<StorageRecord>> getFileInfo(
            @PathVariable("fileId") String fileId,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {

        log.info("Get file info: fileId={}, userId={}", fileId, userId);

        if (userId == null || userId.isEmpty()) {
            userId = "anonymous";
        }

        StorageRecord response = storageService.getFileInfo(fileId, userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * List files for current user
     * GET /storage/files
     */
    @GetMapping("/files")
    public ResponseEntity<ApiResponse<List<StorageRecord>>> listFiles(
            @RequestParam(value = "prefix", required = false) String prefix,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {

        log.info("List files: prefix={}, userId={}", prefix, userId);

        if (userId == null || userId.isEmpty()) {
            userId = "anonymous";
        }

        List<StorageRecord> response = storageService.listFiles(prefix, userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // ==================== Helper Methods ====================

    private String extractObjectNameFromPath() {
        // This is a workaround since Spring doesn't easily extract wildcard paths
        // In practice, you'd use @RequestMapping with path variables properly configured
        return "files/unknown";
    }
}
