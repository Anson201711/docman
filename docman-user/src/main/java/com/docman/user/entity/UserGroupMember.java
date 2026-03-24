package com.docman.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * User group membership
 */
@Data
@TableName("user_group_members")
public class UserGroupMember {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long groupId;

    private Long userId;

    private LocalDateTime createdAt;
}
