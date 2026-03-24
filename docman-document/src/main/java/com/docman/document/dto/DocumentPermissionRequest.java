package com.docman.document.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class DocumentPermissionRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long documentId;

    private String userId;

    private String permissionType;

    private Integer canRead;

    private Integer canWrite;

    private Integer canDelete;

    private Integer canShare;

    private Integer canDownload;
}
