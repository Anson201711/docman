package com.docman.version.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("document_version")
public class DocumentVersion {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    private String documentId;

    private Integer versionNumber;

    private String content;

    private String changeDescription;

    private String createdBy;

    private LocalDateTime createdAt;

    private String status;
}
