package com.docman.cad.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CadFile {
    private Long id;
    private Long documentId;
    private String fileName;
    private String filePath;
    private String format;
    private Long fileSize;
    private Integer version;
    private String thumbnailPath;
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer deleted;
}
