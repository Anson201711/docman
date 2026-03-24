package com.docman.document.service;

import com.docman.document.dto.FolderContentResponse;
import com.docman.document.entity.Folder;
import com.docman.document.dto.FolderCreateRequest;
import com.docman.document.dto.FolderUpdateRequest;

import java.util.List;

public interface FolderService {

    FolderContentResponse getFolderContent(Long id);

    Folder getFolderById(Long id);

    List<Folder> getRootFolders(String ownerId);

    List<Folder> getSubFolders(Long parentId);

    Folder createFolder(FolderCreateRequest request);

    Folder updateFolder(Long id, FolderUpdateRequest request);

    void deleteFolder(Long id);

    String buildFolderPath(Long folderId);

    List<Folder> getAllParentFolders(Long folderId);
}
