package com.docman.document.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.docman.document.dto.*;
import com.docman.document.entity.Document;
import com.docman.document.entity.DocumentPermission;
import com.docman.document.entity.Folder;
import com.docman.document.entity.TrashRecord;
import com.docman.document.repository.DocumentPermissionRepository;
import com.docman.document.repository.DocumentRepository;
import com.docman.document.repository.FolderRepository;
import com.docman.document.repository.TrashRecordRepository;
import com.docman.document.service.DocumentService;
import com.docman.document.service.FolderService;
import com.docman.document.service.TrashService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository documentRepository;
    private final FolderRepository folderRepository;
    private final DocumentPermissionRepository permissionRepository;
    private final TrashRecordRepository trashRecordRepository;
    private final FolderService folderService;
    private final TrashService trashService;

    @Override
    public List<Document> getDocumentList(Long folderId, String ownerId) {
        if (folderId == null) {
            return documentRepository.findByOwnerId(ownerId);
        }
        return documentRepository.findByFolderIdAndOwnerId(folderId, ownerId);
    }

    @Override
    public List<Document> getDocumentsByOwnerId(String ownerId) {
        return documentRepository.findByOwnerId(ownerId);
    }

    @Override
    @Cacheable(value = "document", key = "#id")
    public Document getDocumentById(Long id) {
        return documentRepository.selectById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "document", key = "#result.id")
    public Document createDocument(DocumentCreateRequest request) {
        Document document = new Document();
        document.setName(request.getName());
        document.setContent(request.getContent());
        document.setFileType(request.getFileType());
        document.setFolderId(request.getFolderId());
        document.setOwnerId(request.getOwnerId());
        document.setStatus(1);
        document.setSize(request.getContent() != null ? (long) request.getContent().length() : 0L);
        document.setSortOrder(0);

        String path = "";
        if (request.getFolderId() != null) {
            path = folderService.buildFolderPath(request.getFolderId()) + "/" + request.getName();
        } else {
            path = "/" + request.getName();
        }
        document.setPath(path);

        documentRepository.insert(document);
        return document;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "document", key = "#id")
    public Document updateDocument(Long id, DocumentUpdateRequest request) {
        Document document = documentRepository.selectById(id);
        if (document == null) {
            throw new RuntimeException("Document not found: " + id);
        }

        if (request.getName() != null) {
            document.setName(request.getName());
        }
        if (request.getContent() != null) {
            document.setContent(request.getContent());
            document.setSize((long) request.getContent().length());
        }
        if (request.getFileType() != null) {
            document.setFileType(request.getFileType());
        }
        if (request.getStatus() != null) {
            document.setStatus(request.getStatus());
        }
        if (request.getSortOrder() != null) {
            document.setSortOrder(request.getSortOrder());
        }

        documentRepository.updateById(document);
        return document;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "document", key = "#id")
    public void deleteDocument(Long id) {
        Document document = documentRepository.selectById(id);
        if (document == null) {
            throw new RuntimeException("Document not found: " + id);
        }

        trashService.moveToTrash(id, document.getOwnerId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "document", key = "#id")
    public Document moveDocument(Long id, DocumentMoveRequest request) {
        Document document = documentRepository.selectById(id);
        if (document == null) {
            throw new RuntimeException("Document not found: " + id);
        }

        String oldPath = document.getPath();

        if (request.getTargetFolderId() != null) {
            Folder targetFolder = folderRepository.selectById(request.getTargetFolderId());
            if (targetFolder == null) {
                throw new RuntimeException("Target folder not found: " + request.getTargetFolderId());
            }
            document.setFolderId(request.getTargetFolderId());
            String folderPath = folderService.buildFolderPath(request.getTargetFolderId());
            document.setPath(folderPath + "/" + document.getName());
        } else {
            document.setFolderId(null);
            document.setPath("/" + document.getName());
        }

        documentRepository.updateById(document);

        updateChildDocumentsPath(document);

        log.info("Moved document {} from {} to {}", id, oldPath, document.getPath());
        return document;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "document", key = "#id")
    public Document copyDocument(Long id, DocumentCopyRequest request) {
        Document original = documentRepository.selectById(id);
        if (original == null) {
            throw new RuntimeException("Document not found: " + id);
        }

        Document copy = new Document();
        copy.setName(request.getNewName() != null ? request.getNewName() : original.getName() + "_copy");
        copy.setContent(original.getContent());
        copy.setFileType(original.getFileType());
        copy.setSize(original.getSize());
        copy.setOwnerId(request.getOwnerId());
        copy.setStatus(original.getStatus());
        copy.setSortOrder(original.getSortOrder());

        if (request.getTargetFolderId() != null) {
            Folder targetFolder = folderRepository.selectById(request.getTargetFolderId());
            if (targetFolder == null) {
                throw new RuntimeException("Target folder not found: " + request.getTargetFolderId());
            }
            copy.setFolderId(request.getTargetFolderId());
            String folderPath = folderService.buildFolderPath(request.getTargetFolderId());
            copy.setPath(folderPath + "/" + copy.getName());
        } else {
            copy.setFolderId(null);
            copy.setPath("/" + copy.getName());
        }

        documentRepository.insert(copy);
        log.info("Copied document {} to {}", id, copy.getId());
        return copy;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "document", key = "#id")
    public Document renameDocument(Long id, DocumentRenameRequest request) {
        Document document = documentRepository.selectById(id);
        if (document == null) {
            throw new RuntimeException("Document not found: " + id);
        }

        String oldName = document.getName();
        document.setName(request.getNewName());

        if (document.getFolderId() != null) {
            String folderPath = folderService.buildFolderPath(document.getFolderId());
            document.setPath(folderPath + "/" + request.getNewName());
        } else {
            document.setPath("/" + request.getNewName());
        }

        documentRepository.updateById(document);
        updateChildDocumentsPath(document);

        log.info("Renamed document {} from {} to {}", id, oldName, request.getNewName());
        return document;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "document", key = "#id")
    public void permanentDelete(Long id) {
        trashService.permanentDelete(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "document", key = "#id")
    public Document restoreDocument(Long id) {
        return trashService.restoreFromTrash(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DocumentPermission grantPermission(DocumentPermissionRequest request) {
        DocumentPermission existing = permissionRepository.findByDocumentIdAndUserId(
                request.getDocumentId(), request.getUserId());

        DocumentPermission permission;
        if (existing != null) {
            permission = existing;
            permission.setCanRead(request.getCanRead());
            permission.setCanWrite(request.getCanWrite());
            permission.setCanDelete(request.getCanDelete());
            permission.setCanShare(request.getCanShare());
            permission.setCanDownload(request.getCanDownload());
            permissionRepository.updateById(permission);
        } else {
            permission = new DocumentPermission();
            permission.setDocumentId(request.getDocumentId());
            permission.setUserId(request.getUserId());
            permission.setPermissionType(request.getPermissionType());
            permission.setCanRead(request.getCanRead());
            permission.setCanWrite(request.getCanWrite());
            permission.setCanDelete(request.getCanDelete());
            permission.setCanShare(request.getCanShare());
            permission.setCanDownload(request.getCanDownload());
            permissionRepository.insert(permission);
        }

        return permission;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void revokePermission(Long documentId, String userId) {
        DocumentPermission permission = permissionRepository.findByDocumentIdAndUserId(documentId, userId);
        if (permission != null) {
            permissionRepository.deleteById(permission.getId());
        }
    }

    @Override
    public List<DocumentPermission> getDocumentPermissions(Long documentId) {
        return permissionRepository.findByDocumentId(documentId);
    }

    @Override
    public List<DocumentPermission> getUserPermissions(String userId) {
        return permissionRepository.findByUserId(userId);
    }

    private void updateChildDocumentsPath(Document document) {
        List<Document> children = documentRepository.findByFolderId(document.getId());
        for (Document child : children) {
            child.setPath(document.getPath() + "/" + child.getName());
            documentRepository.updateById(child);
        }
    }
}
