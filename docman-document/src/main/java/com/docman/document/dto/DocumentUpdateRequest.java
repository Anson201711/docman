package com.docman.document.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class DocumentUpdateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private String name;

    private String content;

    private String fileType;

    private Integer status;

    private Integer sortOrder;
}
