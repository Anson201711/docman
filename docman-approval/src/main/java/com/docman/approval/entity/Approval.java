package com.docman.approval.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Approval {
    private Long id;
    private String type;
    private String name;
    private String description;

    private Long documentId;
    private Long templateId;
    private Long applicantId;
    private Long approverId;
    private Long currentApproverId;
    private Integer approvalLevel;
    private Integer totalLevels;

    private String approvalType;
    private String status;
    private String comment;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer deleted;
}
