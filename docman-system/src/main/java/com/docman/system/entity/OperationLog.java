package com.docman.system.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OperationLog {
    private Long id;
    private Long userId;
    private String userName;
    private String module;
    private String operation;
    private String method;
    private String requestUrl;
    private String requestMethod;
    private String requestParams;
    private String responseResult;
    private String ip;
    private String userAgent;
    private Long duration;
    private String status;
    private String errorMessage;
    private LocalDateTime createTime;
}
