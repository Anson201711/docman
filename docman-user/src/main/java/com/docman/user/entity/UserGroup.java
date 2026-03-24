package com.docman.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * User group entity
 */
@Data
@TableName("user_groups")
public class UserGroup {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String groupName;

    private String description;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
