package com.docman.document.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class DocumentCopyRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long targetFolderId;

    private String newName;

    private String ownerId;
}
