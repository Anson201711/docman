-- ============================================
-- DocMan Database Initialization Script
-- ============================================

-- Create databases for each microservice
CREATE DATABASE IF NOT EXISTS docman_user CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS docman_document CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS docman_storage CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS docman_search CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS docman_version CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS docman_classification CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS docman_approval CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS docman_subscription CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS docman_collaboration CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS docman_cad CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS docman_system CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Use the main database
USE docman_user;

-- ============================================
-- Table: users
-- ============================================
CREATE TABLE IF NOT EXISTS `users` (
    `id` VARCHAR(36) NOT NULL COMMENT 'User ID',
    `username` VARCHAR(64) NOT NULL COMMENT 'Username',
    `email` VARCHAR(128) NOT NULL COMMENT 'Email',
    `password` VARCHAR(255) NOT NULL COMMENT 'Password (hashed)',
    `real_name` VARCHAR(64) COMMENT 'Real name',
    `phone` VARCHAR(32) COMMENT 'Phone number',
    `avatar` VARCHAR(512) COMMENT 'Avatar URL',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT 'Status: 0-disabled, 1-enabled',
    `type` TINYINT NOT NULL DEFAULT 1 COMMENT 'Type: 1-internal, 2-external',
    `department` VARCHAR(128) COMMENT 'Department',
    `title` VARCHAR(128) COMMENT 'Job title',
    `storage_used` BIGINT NOT NULL DEFAULT 0 COMMENT 'Storage used (bytes)',
    `storage_quota` BIGINT NOT NULL DEFAULT 5368709120 COMMENT 'Storage quota (default 5GB)',
    `last_login_time` DATETIME COMMENT 'Last login time',
    `last_login_ip` VARCHAR(64) COMMENT 'Last login IP',
    `password_expire_time` DATETIME COMMENT 'Password expiration time',
    `must_change_password` TINYINT NOT NULL DEFAULT 0 COMMENT 'Must change password: 0-no, 1-yes',
    `created_by` VARCHAR(36) COMMENT 'Creator ID',
    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Created time',
    `updated_by` VARCHAR(36) COMMENT 'Updater ID',
    `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Updated time',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT 'Soft delete: 0-not deleted, 1-deleted',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_email` (`email`),
    KEY `idx_status` (`status`),
    KEY `idx_department` (`department`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='User table';

-- ============================================
-- Table: user_groups
-- ============================================
CREATE TABLE IF NOT EXISTS `user_groups` (
    `id` VARCHAR(36) NOT NULL COMMENT 'Group ID',
    `name` VARCHAR(128) NOT NULL COMMENT 'Group name',
    `code` VARCHAR(64) NOT NULL COMMENT 'Group code',
    `description` VARCHAR(512) COMMENT 'Group description',
    `type` TINYINT NOT NULL DEFAULT 1 COMMENT 'Type: 1-department, 2-project, 3-custom',
    `parent_id` VARCHAR(36) COMMENT 'Parent group ID',
    `level` INT NOT NULL DEFAULT 1 COMMENT 'Group level',
    `path` VARCHAR(512) COMMENT 'Group path',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT 'Status: 0-disabled, 1-enabled',
    `created_by` VARCHAR(36) COMMENT 'Creator ID',
    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Created time',
    `updated_by` VARCHAR(36) COMMENT 'Updater ID',
    `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Updated time',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT 'Soft delete: 0-not deleted, 1-deleted',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_code` (`code`),
    KEY `idx_parent_id` (`parent_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='User group table';

-- ============================================
-- Table: user_group_members
-- ============================================
CREATE TABLE IF NOT EXISTS `user_group_members` (
    `id` VARCHAR(36) NOT NULL COMMENT 'Member ID',
    `group_id` VARCHAR(36) NOT NULL COMMENT 'Group ID',
    `user_id` VARCHAR(36) NOT NULL COMMENT 'User ID',
    `role` TINYINT NOT NULL DEFAULT 1 COMMENT 'Role in group: 1-member, 2-admin, 3-owner',
    `joined_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Joined time',
    `created_by` VARCHAR(36) COMMENT 'Creator ID',
    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Created time',
    `updated_by` VARCHAR(36) COMMENT 'Updater ID',
    `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Updated time',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT 'Soft delete: 0-not deleted, 1-deleted',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_group_user` (`group_id`, `user_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_group_id` (`group_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='User group member table';

-- Switch to document database
USE docman_document;

-- ============================================
-- Table: documents
-- ============================================
CREATE TABLE IF NOT EXISTS `documents` (
    `id` VARCHAR(36) NOT NULL COMMENT 'Document ID',
    `name` VARCHAR(255) NOT NULL COMMENT 'Document name',
    `original_name` VARCHAR(255) NOT NULL COMMENT 'Original filename',
    `file_type` VARCHAR(32) NOT NULL COMMENT 'File type extension',
    `mime_type` VARCHAR(128) COMMENT 'MIME type',
    `size` BIGINT NOT NULL DEFAULT 0 COMMENT 'File size (bytes)',
    `storage_key` VARCHAR(512) NOT NULL COMMENT 'Storage path/key in MinIO',
    `folder_id` VARCHAR(36) COMMENT 'Parent folder ID',
    `owner_id` VARCHAR(36) NOT NULL COMMENT 'Owner user ID',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT 'Status: 0-draft, 1-pending, 2-approved, 3-rejected, 4-archived',
    `current_version_id` VARCHAR(36) COMMENT 'Current version ID',
    `version_count` INT NOT NULL DEFAULT 0 COMMENT 'Version count',
    `category_id` VARCHAR(36) COMMENT 'Category ID',
    `classification_level` TINYINT COMMENT 'Classification level: 1-public, 2-internal, 3-confidential, 4-secret',
    `tags` VARCHAR(512) COMMENT 'Tags (JSON array)',
    `description` TEXT COMMENT 'Description',
    `check_sum` VARCHAR(64) COMMENT 'File checksum (SHA-256)',
    `thumbnail_url` VARCHAR(512) COMMENT 'Thumbnail URL',
    `preview_url` VARCHAR(512) COMMENT 'Preview URL',
    `download_count` INT NOT NULL DEFAULT 0 COMMENT 'Download count',
    `view_count` INT NOT NULL DEFAULT 0 COMMENT 'View count',
    `is_locked` TINYINT NOT NULL DEFAULT 0 COMMENT 'Is locked: 0-no, 1-yes',
    `locked_by` VARCHAR(36) COMMENT 'Locked by user ID',
    `locked_time` DATETIME COMMENT 'Locked time',
    `expires_time` DATETIME COMMENT 'Expiration time',
    `created_by` VARCHAR(36) COMMENT 'Creator ID',
    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Created time',
    `updated_by` VARCHAR(36) COMMENT 'Updater ID',
    `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Updated time',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT 'Soft delete: 0-not deleted, 1-deleted',
    PRIMARY KEY (`id`),
    KEY `idx_folder_id` (`folder_id`),
    KEY `idx_owner_id` (`owner_id`),
    KEY `idx_status` (`status`),
    KEY `idx_category_id` (`category_id`),
    KEY `idx_created_time` (`created_time`),
    KEY `idx_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Document table';

-- ============================================
-- Table: folders
-- ============================================
CREATE TABLE IF NOT EXISTS `folders` (
    `id` VARCHAR(36) NOT NULL COMMENT 'Folder ID',
    `name` VARCHAR(255) NOT NULL COMMENT 'Folder name',
    `parent_id` VARCHAR(36) COMMENT 'Parent folder ID',
    `path` VARCHAR(1024) COMMENT 'Folder path',
    `level` INT NOT NULL DEFAULT 1 COMMENT 'Folder level',
    `owner_id` VARCHAR(36) NOT NULL COMMENT 'Owner user ID',
    `group_id` VARCHAR(36) COMMENT 'Owning group ID',
    `is_shared` TINYINT NOT NULL DEFAULT 0 COMMENT 'Is shared: 0-private, 1-shared',
    `share_type` TINYINT COMMENT 'Share type: 1-read, 2-write, 3-manage',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT 'Status: 0-disabled, 1-enabled',
    `color` VARCHAR(16) COMMENT 'Folder color',
    `icon` VARCHAR(64) COMMENT 'Folder icon',
    `document_count` INT NOT NULL DEFAULT 0 COMMENT 'Document count',
    `created_by` VARCHAR(36) COMMENT 'Creator ID',
    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Created time',
    `updated_by` VARCHAR(36) COMMENT 'Updater ID',
    `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Updated time',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT 'Soft delete: 0-not deleted, 1-deleted',
    PRIMARY KEY (`id`),
    KEY `idx_parent_id` (`parent_id`),
    KEY `idx_owner_id` (`owner_id`),
    KEY `idx_group_id` (`group_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Folder table';

-- ============================================
-- Table: document_versions
-- ============================================
CREATE TABLE IF NOT EXISTS `document_versions` (
    `id` VARCHAR(36) NOT NULL COMMENT 'Version ID',
    `document_id` VARCHAR(36) NOT NULL COMMENT 'Document ID',
    `version_number` VARCHAR(32) NOT NULL COMMENT 'Version number (e.g., 1.0, 1.1)',
    `file_size` BIGINT NOT NULL DEFAULT 0 COMMENT 'File size (bytes)',
    `storage_key` VARCHAR(512) NOT NULL COMMENT 'Storage path/key',
    `change_summary` VARCHAR(512) COMMENT 'Change summary',
    `change_description` TEXT COMMENT 'Detailed change description',
    `check_sum` VARCHAR(64) COMMENT 'File checksum',
    `thumbnail_url` VARCHAR(512) COMMENT 'Thumbnail URL',
    `is_current` TINYINT NOT NULL DEFAULT 0 COMMENT 'Is current version: 0-no, 1-yes',
    `is_initial` TINYINT NOT NULL DEFAULT 0 COMMENT 'Is initial version: 0-no, 1-yes',
    `file_object_id` VARCHAR(36) COMMENT 'Original file object ID (for CAD files)',
    `created_by` VARCHAR(36) NOT NULL COMMENT 'Creator ID',
    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Created time',
    `updated_by` VARCHAR(36) COMMENT 'Updater ID',
    `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Updated time',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT 'Soft delete: 0-not deleted, 1-deleted',
    PRIMARY KEY (`id`),
    KEY `idx_document_id` (`document_id`),
    KEY `idx_created_by` (`created_by`),
    KEY `idx_created_time` (`created_time`),
    KEY `idx_is_current` (`is_current`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Document version table';

-- Switch to classification database
USE docman_classification;

-- ============================================
-- Table: categories
-- ============================================
CREATE TABLE IF NOT EXISTS `categories` (
    `id` VARCHAR(36) NOT NULL COMMENT 'Category ID',
    `name` VARCHAR(128) NOT NULL COMMENT 'Category name',
    `code` VARCHAR(64) NOT NULL COMMENT 'Category code',
    `parent_id` VARCHAR(36) COMMENT 'Parent category ID',
    `level` INT NOT NULL DEFAULT 1 COMMENT 'Category level',
    `path` VARCHAR(512) COMMENT 'Category path',
    `icon` VARCHAR(64) COMMENT 'Category icon',
    `color` VARCHAR(16) COMMENT 'Category color',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT 'Sort order',
    `description` VARCHAR(512) COMMENT 'Description',
    `document_count` INT NOT NULL DEFAULT 0 COMMENT 'Document count',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT 'Status: 0-disabled, 1-enabled',
    `created_by` VARCHAR(36) COMMENT 'Creator ID',
    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Created time',
    `updated_by` VARCHAR(36) COMMENT 'Updater ID',
    `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Updated time',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT 'Soft delete: 0-not deleted, 1-deleted',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_code` (`code`),
    KEY `idx_parent_id` (`parent_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Category table';

-- ============================================
-- Table: document_category (many-to-many)
-- ============================================
CREATE TABLE IF NOT EXISTS `document_category` (
    `id` VARCHAR(36) NOT NULL COMMENT 'ID',
    `document_id` VARCHAR(36) NOT NULL COMMENT 'Document ID',
    `category_id` VARCHAR(36) NOT NULL COMMENT 'Category ID',
    `created_by` VARCHAR(36) COMMENT 'Creator ID',
    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Created time',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT 'Soft delete: 0-not deleted, 1-deleted',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_document_category` (`document_id`, `category_id`),
    KEY `idx_document_id` (`document_id`),
    KEY `idx_category_id` (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Document category mapping table';

USE docman_document;

-- ============================================
-- Table: permissions
-- ============================================
CREATE TABLE IF NOT EXISTS `permissions` (
    `id` VARCHAR(36) NOT NULL COMMENT 'Permission ID',
    `resource_type` TINYINT NOT NULL COMMENT 'Resource type: 1-document, 2-folder, 3-category',
    `resource_id` VARCHAR(36) NOT NULL COMMENT 'Resource ID',
    `principal_type` TINYINT NOT NULL COMMENT 'Principal type: 1-user, 2-group',
    `principal_id` VARCHAR(36) NOT NULL COMMENT 'Principal ID (user or group)',
    `permission` VARCHAR(32) NOT NULL COMMENT 'Permission: READ, WRITE, DELETE, MANAGE',
    `inherited` TINYINT NOT NULL DEFAULT 0 COMMENT 'Is inherited: 0-no, 1-yes',
    `granted_by` VARCHAR(36) COMMENT 'Granted by user ID',
    `granted_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Granted time',
    `expires_time` DATETIME COMMENT 'Expiration time',
    `created_by` VARCHAR(36) COMMENT 'Creator ID',
    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Created time',
    `updated_by` VARCHAR(36) COMMENT 'Updater ID',
    `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Updated time',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT 'Soft delete: 0-not deleted, 1-deleted',
    PRIMARY KEY (`id`),
    KEY `idx_resource` (`resource_type`, `resource_id`),
    KEY `idx_principal` (`principal_type`, `principal_id`),
    KEY `idx_principal_resource` (`principal_id`, `resource_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Permission table';

-- ============================================
-- Table: trash
-- ============================================
CREATE TABLE IF NOT EXISTS `trash` (
    `id` VARCHAR(36) NOT NULL COMMENT 'Trash record ID',
    `document_id` VARCHAR(36) NOT NULL COMMENT 'Document ID',
    `name` VARCHAR(255) NOT NULL COMMENT 'Document name at delete time',
    `file_type` VARCHAR(32) NOT NULL COMMENT 'File type',
    `size` BIGINT NOT NULL DEFAULT 0 COMMENT 'File size',
    `storage_key` VARCHAR(512) NOT NULL COMMENT 'Storage path',
    `owner_id` VARCHAR(36) NOT NULL COMMENT 'Original owner',
    `deleted_by` VARCHAR(36) NOT NULL COMMENT 'User who deleted',
    `deleted_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Deleted time',
    `expires_time` DATETIME NOT NULL COMMENT 'Expires (auto-delete) time',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT 'Status: 0-in-trash, 1-restoring, 2-restored',
    `restored_to` VARCHAR(36) COMMENT 'Restored folder ID',
    `restored_time` DATETIME COMMENT 'Restored time',
    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Created time',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT 'Soft delete: 0-not deleted, 1-deleted',
    PRIMARY KEY (`id`),
    KEY `idx_document_id` (`document_id`),
    KEY `idx_owner_id` (`owner_id`),
    KEY `idx_deleted_by` (`deleted_by`),
    KEY `idx_expires_time` (`expires_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Trash table';

-- Switch to approval database
USE docman_approval;

-- ============================================
-- Table: approval_templates
-- ============================================
CREATE TABLE IF NOT EXISTS `approval_templates` (
    `id` VARCHAR(36) NOT NULL COMMENT 'Template ID',
    `name` VARCHAR(128) NOT NULL COMMENT 'Template name',
    `code` VARCHAR(64) NOT NULL COMMENT 'Template code',
    `description` VARCHAR(512) COMMENT 'Description',
    `applicable_type` TINYINT NOT NULL DEFAULT 1 COMMENT 'Applicable type: 1-document, 2-folder, 3-category',
    `applicable_scope` VARCHAR(512) COMMENT 'Applicable scope (JSON: category IDs or file types)',
    `approval_type` TINYINT NOT NULL COMMENT 'Approval type: 1-serial, 2-parallel, 3-any',
    `approval_steps` TEXT NOT NULL COMMENT 'Approval steps (JSON array)',
    `auto_approve_conditions` TEXT COMMENT 'Auto approve conditions (JSON)',
    `timeout_hours` INT COMMENT 'Timeout hours per step',
    `timeout_action` TINYINT COMMENT 'Timeout action: 1-skip, 2-remind, 3-auto-approve',
    `remind_interval_hours` INT COMMENT 'Remind interval hours',
    `can_withdraw` TINYINT NOT NULL DEFAULT 1 COMMENT 'Can withdraw: 0-no, 1-yes',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT 'Status: 0-disabled, 1-enabled',
    `version` INT NOT NULL DEFAULT 1 COMMENT 'Template version',
    `created_by` VARCHAR(36) COMMENT 'Creator ID',
    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Created time',
    `updated_by` VARCHAR(36) COMMENT 'Updater ID',
    `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Updated time',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT 'Soft delete: 0-not deleted, 1-deleted',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_code_version` (`code`, `version`),
    KEY `idx_status` (`status`),
    KEY `idx_applicable_type` (`applicable_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Approval template table';

-- ============================================
-- Table: approval_instances
-- ============================================
CREATE TABLE IF NOT EXISTS `approval_instances` (
    `id` VARCHAR(36) NOT NULL COMMENT 'Instance ID',
    `template_id` VARCHAR(36) NOT NULL COMMENT 'Template ID',
    `document_id` VARCHAR(36) NOT NULL COMMENT 'Document ID',
    `title` VARCHAR(256) NOT NULL COMMENT 'Approval title',
    `description` TEXT COMMENT 'Description',
    `applicant_id` VARCHAR(36) NOT NULL COMMENT 'Applicant user ID',
    `current_step` INT NOT NULL DEFAULT 1 COMMENT 'Current step number',
    `total_steps` INT NOT NULL DEFAULT 1 COMMENT 'Total steps',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT 'Status: 0-pending, 1-in-progress, 2-approved, 3-rejected, 4-withdrawn, 5-timeout',
    `started_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Started time',
    `completed_time` DATETIME COMMENT 'Completed time',
    `withdraw_time` DATETIME COMMENT 'Withdrawn time',
    `withdraw_reason` VARCHAR(512) COMMENT 'Withdraw reason',
    `approval_deadline` DATETIME COMMENT 'Approval deadline',
    `created_by` VARCHAR(36) COMMENT 'Creator ID',
    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Created time',
    `updated_by` VARCHAR(36) COMMENT 'Updater ID',
    `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Updated time',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT 'Soft delete: 0-not deleted, 1-deleted',
    PRIMARY KEY (`id`),
    KEY `idx_template_id` (`template_id`),
    KEY `idx_document_id` (`document_id`),
    KEY `idx_applicant_id` (`applicant_id`),
    KEY `idx_status` (`status`),
    KEY `idx_started_time` (`started_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Approval instance table';

-- ============================================
-- Table: approval_records
-- ============================================
CREATE TABLE IF NOT EXISTS `approval_records` (
    `id` VARCHAR(36) NOT NULL COMMENT 'Record ID',
    `instance_id` VARCHAR(36) NOT NULL COMMENT 'Approval instance ID',
    `step` INT NOT NULL COMMENT 'Step number',
    `approver_id` VARCHAR(36) NOT NULL COMMENT 'Approver user ID',
    `action` TINYINT NOT NULL COMMENT 'Action: 1-approve, 2-reject, 3-transfer',
    `comment` TEXT COMMENT 'Comment',
    `transfer_to` VARCHAR(36) COMMENT 'Transfer to user ID',
    `transfer_reason` VARCHAR(512) COMMENT 'Transfer reason',
    `action_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Action time',
    `is_current` TINYINT NOT NULL DEFAULT 1 COMMENT 'Is current step: 0-no, 1-yes',
    `created_by` VARCHAR(36) COMMENT 'Creator ID',
    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Created time',
    `updated_by` VARCHAR(36) COMMENT 'Updater ID',
    `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Updated time',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT 'Soft delete: 0-not deleted, 1-deleted',
    PRIMARY KEY (`id`),
    KEY `idx_instance_id` (`instance_id`),
    KEY `idx_approver_id` (`approver_id`),
    KEY `idx_action_time` (`action_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Approval record table';

-- Switch to subscription database
USE docman_subscription;

-- ============================================
-- Table: subscriptions
-- ============================================
CREATE TABLE IF NOT EXISTS `subscriptions` (
    `id` VARCHAR(36) NOT NULL COMMENT 'Subscription ID',
    `user_id` VARCHAR(36) NOT NULL COMMENT 'User ID',
    `subscription_type` TINYINT NOT NULL COMMENT 'Subscription type: 1-document, 2-folder, 3-category',
    `resource_id` VARCHAR(36) NOT NULL COMMENT 'Resource ID',
    `notification_method` TINYINT NOT NULL DEFAULT 1 COMMENT 'Notification method: 1-in-app, 2-email, 3-both',
    `frequency` TINYINT NOT NULL DEFAULT 1 COMMENT 'Frequency: 1-immediate, 2-daily digest, 3-off',
    `event_types` VARCHAR(128) NOT NULL COMMENT 'Event types to subscribe (JSON array)',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT 'Status: 0-disabled, 1-enabled',
    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Created time',
    `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Updated time',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT 'Soft delete: 0-not deleted, 1-deleted',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_resource` (`user_id`, `subscription_type`, `resource_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_resource` (`subscription_type`, `resource_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Subscription table';

-- ============================================
-- Table: notifications
-- ============================================
CREATE TABLE IF NOT EXISTS `notifications` (
    `id` VARCHAR(36) NOT NULL COMMENT 'Notification ID',
    `user_id` VARCHAR(36) NOT NULL COMMENT 'User ID',
    `type` TINYINT NOT NULL COMMENT 'Type: 1-document update, 2-approval, 3-comment, 4-share, 5-system',
    `title` VARCHAR(256) NOT NULL COMMENT 'Title',
    `content` TEXT COMMENT 'Content',
    `resource_type` TINYINT COMMENT 'Related resource type',
    `resource_id` VARCHAR(36) COMMENT 'Related resource ID',
    `is_read` TINYINT NOT NULL DEFAULT 0 COMMENT 'Is read: 0-unread, 1-read',
    `read_time` DATETIME COMMENT 'Read time',
    `action_url` VARCHAR(512) COMMENT 'Action URL',
    `priority` TINYINT NOT NULL DEFAULT 0 COMMENT 'Priority: 0-normal, 1-high, 2-urgent',
    `expires_time` DATETIME COMMENT 'Expires time',
    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Created time',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT 'Soft delete: 0-not deleted, 1-deleted',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_is_read` (`is_read`),
    KEY `idx_created_time` (`created_time`),
    KEY `idx_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Notification table';

-- Switch to system database
USE docman_system;

-- ============================================
-- Table: operation_logs
-- ============================================
CREATE TABLE IF NOT EXISTS `operation_logs` (
    `id` VARCHAR(36) NOT NULL COMMENT 'Log ID',
    `user_id` VARCHAR(36) COMMENT 'User ID',
    `username` VARCHAR(64) COMMENT 'Username',
    `operation` VARCHAR(64) NOT NULL COMMENT 'Operation type',
    `resource_type` TINYINT COMMENT 'Resource type',
    `resource_id` VARCHAR(36) COMMENT 'Resource ID',
    `resource_name` VARCHAR(256) COMMENT 'Resource name',
    `method` VARCHAR(16) COMMENT 'HTTP method',
    `url` VARCHAR(512) COMMENT 'Request URL',
    `ip` VARCHAR(64) COMMENT 'Client IP',
    `location` VARCHAR(256) COMMENT 'Location',
    `user_agent` VARCHAR(512) COMMENT 'User agent',
    `request_params` TEXT COMMENT 'Request parameters',
    `response_code` INT COMMENT 'Response code',
    `error_message` TEXT COMMENT 'Error message',
    `duration` BIGINT COMMENT 'Duration (ms)',
    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Created time',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_operation` (`operation`),
    KEY `idx_resource` (`resource_type`, `resource_id`),
    KEY `idx_created_time` (`created_time`),
    KEY `idx_ip` (`ip`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Operation log table';

-- ============================================
-- Table: system_config
-- ============================================
CREATE TABLE IF NOT EXISTS `system_config` (
    `id` VARCHAR(36) NOT NULL COMMENT 'Config ID',
    `config_key` VARCHAR(128) NOT NULL COMMENT 'Config key',
    `config_value` TEXT COMMENT 'Config value',
    `config_type` VARCHAR(32) NOT NULL DEFAULT 'string' COMMENT 'Config type: string, number, boolean, json',
    `group_name` VARCHAR(64) COMMENT 'Group name',
    `description` VARCHAR(512) COMMENT 'Description',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT 'Sort order',
    `editable` TINYINT NOT NULL DEFAULT 1 COMMENT 'Is editable: 0-no, 1-yes',
    `visible` TINYINT NOT NULL DEFAULT 1 COMMENT 'Is visible: 0-no, 1-yes',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT 'Status: 0-disabled, 1-enabled',
    `created_by` VARCHAR(36) COMMENT 'Creator ID',
    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Created time',
    `updated_by` VARCHAR(36) COMMENT 'Updater ID',
    `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Updated time',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_config_key` (`config_key`),
    KEY `idx_group_name` (`group_name`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='System config table';

-- ============================================
-- Insert default system config
-- ============================================
INSERT INTO `docman_system`.`system_config` (`id`, `config_key`, `config_value`, `config_type`, `group_name`, `description`, `sort_order`, `editable`, `visible`) VALUES
('1', 'system.name', 'DocMan Document Management System', 'string', 'system', 'System name', 1, 1, 1),
('2', 'system.version', '1.0.0', 'string', 'system', 'System version', 2, 0, 1),
('3', 'storage.max_file_size', '104857600', 'number', 'storage', 'Max file size (bytes)', 10, 1, 1),
('4', 'storage.allowed_types', '.pdf,.doc,.docx,.xls,.xlsx,.ppt,.pptx,.txt,.jpg,.jpeg,.png,.gif,.zip', 'string', 'storage', 'Allowed file types', 11, 1, 1),
('5', 'storage.default_quota', '5368709120', 'number', 'storage', 'Default storage quota per user (bytes)', 12, 1, 1),
('6', 'approval.default_timeout_hours', '72', 'number', 'approval', 'Default approval timeout (hours)', 20, 1, 1),
('7', 'trash.retention_days', '30', 'number', 'trash', 'Trash retention days', 30, 1, 1),
('8', 'elasticsearch.enabled', 'true', 'boolean', 'search', 'Enable Elasticsearch', 40, 1, 1),
('9', 'minio.endpoint', 'http://minio:9000', 'string', 'storage', 'MinIO endpoint', 13, 1, 1),
('10', 'jwt.expiration', '900000', 'number', 'security', 'JWT token expiration (ms)', 50, 1, 1),
('11', 'jwt.refresh_expiration', '604800000', 'number', 'security', 'JWT refresh token expiration (ms)', 51, 1, 1);

-- ============================================
-- Insert default admin user (password: root123)
-- ============================================
INSERT INTO `docman_user`.`users` (`id`, `username`, `email`, `password`, `real_name`, `status`, `type`, `created_by`, `created_time`) VALUES
('1', 'admin', 'admin@docman.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'System Administrator', 1, 1, 'system', NOW());

-- ============================================
-- Insert default admin group
-- ============================================
INSERT INTO `docman_user`.`user_groups` (`id`, `name`, `code`, `description`, `type`, `level`, `status`, `created_by`, `created_time`) VALUES
('1', 'Administrators', 'ADMIN', 'System administrators group', 3, 1, 1, 'system', NOW());

-- ============================================
-- Add admin to admin group
-- ============================================
INSERT INTO `docman_user`.`user_group_members` (`id`, `group_id`, `user_id`, `role`, `created_by`, `created_time`) VALUES
('1', '1', '1', 3, 'system', NOW());

-- ============================================
-- Insert default category
-- ============================================
INSERT INTO `docman_classification`.`categories` (`id`, `name`, `code`, `level`, `sort_order`, `status`, `created_by`, `created_time`) VALUES
('1', 'General Documents', 'GENERAL', 1, 1, 1, 'system', NOW()),
('2', 'Project Documents', 'PROJECT', 1, 2, 1, 'system', NOW()),
('3', 'Technical Documents', 'TECH', 1, 3, 1, 'system', NOW());
