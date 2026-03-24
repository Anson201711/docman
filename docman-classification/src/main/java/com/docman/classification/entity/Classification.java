package com.docman.classification.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("classification")
public class Classification {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    private String parentId;

    private String name;

    private String code;

    private String description;

    private Integer sortOrder;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private String status;
}
