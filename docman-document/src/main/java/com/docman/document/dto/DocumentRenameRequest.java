package com.docman.document.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class DocumentRenameRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private String newName;

    private String ownerId;
}
