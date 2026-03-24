-- Document Management Database Schema
-- Database: docman_document

CREATE DATABASE IF NOT EXISTS docman_document DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE docman_document;

-- Document Table
CREATE TABLE IF NOT EXISTS `document` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'Document ID',
    `name` VARCHAR(255) NOT NULL COMMENT 'Document name',
    `content` LONGTEXT COMMENT 'Document content',
    `file_type` VARCHAR(50) COMMENT 'File type (txt, md, pdf, etc.)',
    `size` BIGINT DEFAULT 0 COMMENT 'Document size in bytes',
    `folder_id` BIGINT COMMENT 'Parent folder ID, NULL for root level',
    `path` VARCHAR(1000) COMMENT 'Full path of the document',
    `owner_id` VARCHAR(64) NOT NULL COMMENT 'Owner user ID',
    `status` INT DEFAULT 1 COMMENT 'Status: 1=active, 0=archived',
    `sort_order` INT DEFAULT 0 COMMENT 'Sort order',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
    `deleted` TINYINT DEFAULT 0 COMMENT 'Soft delete flag: 0=not deleted, 1=deleted',
    INDEX `idx_folder_id` (`folder_id`),
    INDEX `idx_owner_id` (`owner_id`),
    INDEX `idx_path` (`path`),
    INDEX `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Document table';

-- Folder Table
CREATE TABLE IF NOT EXISTS `folder` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'Folder ID',
    `name` VARCHAR(255) NOT NULL COMMENT 'Folder name',
    `parent_id` BIGINT DEFAULT 0 COMMENT 'Parent folder ID, 0 for root level',
    `path` VARCHAR(1000) COMMENT 'Full path of the folder',
    `owner_id` VARCHAR(64) NOT NULL COMMENT 'Owner user ID',
    `sort_order` INT DEFAULT 0 COMMENT 'Sort order',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
    `deleted` TINYINT DEFAULT 0 COMMENT 'Soft delete flag: 0=not deleted, 1=deleted',
    INDEX `idx_parent_id` (`parent_id`),
    INDEX `idx_owner_id` (`owner_id`),
    INDEX `idx_path` (`path`),
    INDEX `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Folder table';

-- Document Permission Table
CREATE TABLE IF NOT EXISTS `document_permission` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'Permission ID',
    `document_id` BIGINT NOT NULL COMMENT 'Document ID',
    `user_id` VARCHAR(64) NOT NULL COMMENT 'User ID granted permission',
    `permission_type` VARCHAR(20) DEFAULT 'custom' COMMENT 'Permission type: read, write, admin, custom',
    `can_read` TINYINT DEFAULT 1 COMMENT 'Read permission: 0=no, 1=yes',
    `can_write` TINYINT DEFAULT 0 COMMENT 'Write permission: 0=no, 1=yes',
    `can_delete` TINYINT DEFAULT 0 COMMENT 'Delete permission: 0=no, 1=yes',
    `can_share` TINYINT DEFAULT 0 COMMENT 'Share permission: 0=no, 1=yes',
    `can_download` TINYINT DEFAULT 0 COMMENT 'Download permission: 0=no, 1=yes',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
    `deleted` TINYINT DEFAULT 0 COMMENT 'Soft delete flag: 0=not deleted, 1=deleted',
    INDEX `idx_document_id` (`document_id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Document permission table';

-- Trash Record Table
CREATE TABLE IF NOT EXISTS `trash_record` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'Record ID',
    `document_id` BIGINT NOT NULL COMMENT 'Original document ID',
    `document_name` VARCHAR(255) NOT NULL COMMENT 'Original document name',
    `folder_id` BIGINT COMMENT 'Original parent folder ID',
    `folder_path` VARCHAR(1000) COMMENT 'Original folder path',
    `owner_id` VARCHAR(64) NOT NULL COMMENT 'Owner user ID',
    `record_type` INT DEFAULT 1 COMMENT 'Record type: 1=document, 2=folder',
    `original_status` INT COMMENT 'Original document status',
    `delete_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Delete time',
    `expire_time` DATETIME NOT NULL COMMENT 'Expiration time for auto cleanup',
    `deleted` TINYINT DEFAULT 0 COMMENT 'Soft delete flag: 0=not deleted, 1=deleted',
    INDEX `idx_document_id` (`document_id`),
    INDEX `idx_owner_id` (`owner_id`),
    INDEX `idx_expire_time` (`expire_time`),
    INDEX `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Trash record table';
