package com.docman.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.docman.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * User entity
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("users")
public class User extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String username;

    private String passwordHash;

    private String email;

    private String displayName;

    private String avatarUrl;

    /**
     * 0: disabled, 1: normal
     */
    private Integer status;

    /**
     * USER, ADMIN
     */
    private String role;

    /**
     * Storage quota in bytes, default 5GB
     */
    private Long storageQuota;

    /**
     * Used storage in bytes
     */
    private Long storageUsed;

    private String lastLoginIp;

    private java.time.LocalDateTime lastLoginAt;
}
