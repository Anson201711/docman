-- Storage Service Database Schema
-- Database: docman_storage

-- Storage Quota Table
CREATE TABLE IF NOT EXISTS storage_quota (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'Primary key',
    user_id VARCHAR(64) NOT NULL COMMENT 'User ID',
    max_quota BIGINT NOT NULL DEFAULT 10737418240 COMMENT 'Maximum storage quota in bytes (default 10GB)',
    used_quota BIGINT NOT NULL DEFAULT 0 COMMENT 'Currently used storage in bytes',
    active INT NOT NULL DEFAULT 1 COMMENT 'Flag indicating if quota is active (1=active, 0=inactive)',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation time',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last update time',
    UNIQUE KEY uk_user_id (user_id),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Storage quota table';

-- Storage Record Table
CREATE TABLE IF NOT EXISTS storage_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'Primary key',
    file_id VARCHAR(64) NOT NULL COMMENT 'File unique identifier (UUID)',
    file_name VARCHAR(255) NOT NULL COMMENT 'Original filename',
    extension VARCHAR(32) NOT NULL COMMENT 'File extension',
    content_type VARCHAR(128) NOT NULL COMMENT 'MIME type',
    file_size BIGINT NOT NULL COMMENT 'File size in bytes',
    object_name VARCHAR(512) NOT NULL COMMENT 'MinIO object name (path in bucket)',
    bucket_name VARCHAR(128) NOT NULL DEFAULT 'docman' COMMENT 'Bucket name',
    checksum VARCHAR(64) COMMENT 'File checksum (MD5)',
    user_id VARCHAR(64) NOT NULL COMMENT 'Owner user ID',
    document_id VARCHAR(64) COMMENT 'Associated document ID',
    quota_allocated BIGINT COMMENT 'Storage quota allocated at upload time',
    quota_used BIGINT COMMENT 'Quota used at upload time',
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING' COMMENT 'Upload status: PENDING, UPLOADING, COMPLETED, FAILED',
    upload_id VARCHAR(128) COMMENT 'Multipart upload ID (if applicable)',
    deleted INT NOT NULL DEFAULT 0 COMMENT 'Flag indicating if deleted (0=not deleted, 1=deleted)',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation time',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last update time',
    delete_time DATETIME COMMENT 'Deletion time (if deleted)',
    UNIQUE KEY uk_file_id (file_id),
    KEY idx_user_id (user_id),
    KEY idx_document_id (document_id),
    KEY idx_object_name (object_name),
    KEY idx_status (status),
    KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Storage record table';

-- Multipart Upload Table
CREATE TABLE IF NOT EXISTS multipart_upload (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'Primary key',
    upload_id VARCHAR(128) NOT NULL COMMENT 'Multipart upload ID from MinIO',
    file_id VARCHAR(64) NOT NULL COMMENT 'File ID (UUID)',
    file_name VARCHAR(255) NOT NULL COMMENT 'Original filename',
    extension VARCHAR(32) NOT NULL COMMENT 'File extension',
    total_size BIGINT NOT NULL COMMENT 'Total file size in bytes',
    part_size BIGINT NOT NULL COMMENT 'Part size in bytes',
    total_parts INT NOT NULL COMMENT 'Total number of parts',
    uploaded_parts INT NOT NULL DEFAULT 0 COMMENT 'Number of parts uploaded',
    content_type VARCHAR(128) NOT NULL COMMENT 'MIME type',
    user_id VARCHAR(64) NOT NULL COMMENT 'Owner user ID',
    bucket_name VARCHAR(128) NOT NULL COMMENT 'Bucket name',
    object_name VARCHAR(512) NOT NULL COMMENT 'Object name in MinIO',
    status VARCHAR(32) NOT NULL DEFAULT 'INIT' COMMENT 'Upload status: INIT, UPLOADING, COMPLETED, CANCELLED, EXPIRED',
    deleted INT NOT NULL DEFAULT 0 COMMENT 'Flag indicating if deleted',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation time',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last update time',
    expire_time DATETIME NOT NULL COMMENT 'Expiration time',
    UNIQUE KEY uk_upload_id (upload_id),
    KEY idx_file_id (file_id),
    KEY idx_user_id (user_id),
    KEY idx_status (status),
    KEY idx_expire_time (expire_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Multipart upload tracking table';
