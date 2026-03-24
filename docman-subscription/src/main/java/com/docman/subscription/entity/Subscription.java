package com.docman.subscription.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Subscription {
    private Long id;
    private Long userId;
    private Long documentId;
    private String documentName;
    private String eventType;
    private String status;
    private String notificationChannel;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer deleted;
}
