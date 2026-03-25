package com.docman.document.dto;

import com.docman.document.entity.Document;
import com.docman.document.entity.Folder;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class FolderContentResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long folderId;

    private String folderName;

    private String folderPath;

    private List<Folder> subFolders;

    private List<Document> documents;
}
