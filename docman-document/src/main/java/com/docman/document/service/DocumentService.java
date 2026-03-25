package com.docman.document.service;

import com.docman.document.dto.*;
import com.docman.document.entity.Document;
import com.docman.document.entity.DocumentPermission;

import java.util.List;

public interface DocumentService {

    List<Document> getDocumentList(Long folderId, String ownerId);

    List<Document> getDocumentsByOwnerId(String ownerId);

    Document getDocumentById(Long id);

    Document createDocument(DocumentCreateRequest request);

    Document updateDocument(Long id, DocumentUpdateRequest request);

    void deleteDocument(Long id);

    Document moveDocument(Long id, DocumentMoveRequest request);

    Document copyDocument(Long id, DocumentCopyRequest request);

    Document renameDocument(Long id, DocumentRenameRequest request);

    void permanentDelete(Long id);

    Document restoreDocument(Long id);

    DocumentPermission grantPermission(DocumentPermissionRequest request);

    void revokePermission(Long documentId, String userId);

    List<DocumentPermission> getDocumentPermissions(Long documentId);

    List<DocumentPermission> getUserPermissions(String userId);
}
