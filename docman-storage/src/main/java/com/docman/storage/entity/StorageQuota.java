package com.docman.storage.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Storage Quota Entity
 * Manages storage quotas for users
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("storage_quota")
public class StorageQuota {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * User ID
     */
    private String userId;

    /**
     * Maximum storage quota in bytes
     */
    private Long maxQuota;

    /**
     * Currently used storage in bytes
     */
    private Long usedQuota;

    /**
     * Flag indicating if quota is active
     */
    private Integer active;

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
}
