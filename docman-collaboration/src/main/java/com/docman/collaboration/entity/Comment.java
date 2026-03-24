package com.docman.collaboration.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Comment {
    private Long id;
    private Long documentId;
    private Long userId;
    private String userName;
    private Long parentId;
    private String content;
    private String type;
    private String position;
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer deleted;
}
