package com.docman.collaboration.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserStatus {
    private Long userId;
    private String userName;
    private Long documentId;
    private String status;
    private LocalDateTime lastActiveTime;
}
