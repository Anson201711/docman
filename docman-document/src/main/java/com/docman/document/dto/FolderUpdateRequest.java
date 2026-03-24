package com.docman.document.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class FolderUpdateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private String name;

    private Integer sortOrder;
}
