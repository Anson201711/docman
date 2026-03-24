package com.docman.subscription.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Notification {
    private Long id;
    private Long userId;
    private Long subscriptionId;
    private String type;
    private String title;
    private String content;
    private String status;
    private String readStatus;
    private LocalDateTime createTime;
    private LocalDateTime readTime;
    private Integer deleted;
}
