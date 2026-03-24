package com.docman.classification.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("document_classification")
public class DocumentClassification {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    private String documentId;

    private String classificationId;

    private LocalDateTime createdAt;

    private String status;
}
