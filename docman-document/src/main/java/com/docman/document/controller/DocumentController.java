package com.docman.document.controller;

import com.docman.document.dto.*;
import com.docman.document.entity.Document;
import com.docman.document.entity.DocumentPermission;
import com.docman.document.service.DocumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @GetMapping
    public ApiResponse<List<Document>> getDocumentList(
            @RequestParam(required = false) Long folderId,
            @RequestParam(required = false) String ownerId) {
        try {
            List<Document> documents = documentService.getDocumentList(folderId, ownerId);
            return ApiResponse.success(documents);
        } catch (Exception e) {
            log.error("Failed to get document list", e);
            return ApiResponse.error(e.getMessage());
        }
    }

    @PostMapping
    public ApiResponse<Document> createDocument(@RequestBody DocumentCreateRequest request) {
        try {
            Document document = documentService.createDocument(request);
            return ApiResponse.success("Document created successfully", document);
        } catch (Exception e) {
            log.error("Failed to create document", e);
            return ApiResponse.error(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ApiResponse<Document> getDocument(@PathVariable Long id) {
        try {
            Document document = documentService.getDocumentById(id);
            if (document == null) {
                return ApiResponse.error(404, "Document not found");
            }
            return ApiResponse.success(document);
        } catch (Exception e) {
            log.error("Failed to get document", e);
            return ApiResponse.error(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ApiResponse<Document> updateDocument(
            @PathVariable Long id,
            @RequestBody DocumentUpdateRequest request) {
        try {
            Document document = documentService.updateDocument(id, request);
            return ApiResponse.success("Document updated successfully", document);
        } catch (Exception e) {
            log.error("Failed to update document", e);
            return ApiResponse.error(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteDocument(@PathVariable Long id) {
        try {
            documentService.deleteDocument(id);
            return ApiResponse.success("Document moved to trash", null);
        } catch (Exception e) {
            log.error("Failed to delete document", e);
            return ApiResponse.error(e.getMessage());
        }
    }

    @PostMapping("/{id}/move")
    public ApiResponse<Document> moveDocument(
            @PathVariable Long id,
            @RequestBody DocumentMoveRequest request) {
        try {
            Document document = documentService.moveDocument(id, request);
            return ApiResponse.success("Document moved successfully", document);
        } catch (Exception e) {
            log.error("Failed to move document", e);
            return ApiResponse.error(e.getMessage());
        }
    }

    @PostMapping("/{id}/copy")
    public ApiResponse<Document> copyDocument(
            @PathVariable Long id,
            @RequestBody DocumentCopyRequest request) {
        try {
            Document document = documentService.copyDocument(id, request);
            return ApiResponse.success("Document copied successfully", document);
        } catch (Exception e) {
            log.error("Failed to copy document", e);
            return ApiResponse.error(e.getMessage());
        }
    }

    @PostMapping("/{id}/rename")
    public ApiResponse<Document> renameDocument(
            @PathVariable Long id,
            @RequestBody DocumentRenameRequest request) {
        try {
            Document document = documentService.renameDocument(id, request);
            return ApiResponse.success("Document renamed successfully", document);
        } catch (Exception e) {
            log.error("Failed to rename document", e);
            return ApiResponse.error(e.getMessage());
        }
    }

    @DeleteMapping("/{id}/permanent")
    public ApiResponse<Void> permanentDelete(@PathVariable Long id) {
        try {
            documentService.permanentDelete(id);
            return ApiResponse.success("Document permanently deleted", null);
        } catch (Exception e) {
            log.error("Failed to permanently delete document", e);
            return ApiResponse.error(e.getMessage());
        }
    }

    @PostMapping("/{id}/restore")
    public ApiResponse<Document> restoreDocument(@PathVariable Long id) {
        try {
            Document document = documentService.restoreDocument(id);
            return ApiResponse.success("Document restored successfully", document);
        } catch (Exception e) {
            log.error("Failed to restore document", e);
            return ApiResponse.error(e.getMessage());
        }
    }

    @PostMapping("/permissions")
    public ApiResponse<DocumentPermission> grantPermission(
            @RequestBody DocumentPermissionRequest request) {
        try {
            DocumentPermission permission = documentService.grantPermission(request);
            return ApiResponse.success("Permission granted successfully", permission);
        } catch (Exception e) {
            log.error("Failed to grant permission", e);
            return ApiResponse.error(e.getMessage());
        }
    }

    @DeleteMapping("/{id}/permissions/{userId}")
    public ApiResponse<Void> revokePermission(
            @PathVariable Long id,
            @PathVariable String userId) {
        try {
            documentService.revokePermission(id, userId);
            return ApiResponse.success("Permission revoked successfully", null);
        } catch (Exception e) {
            log.error("Failed to revoke permission", e);
            return ApiResponse.error(e.getMessage());
        }
    }

    @GetMapping("/{id}/permissions")
    public ApiResponse<List<DocumentPermission>> getDocumentPermissions(@PathVariable Long id) {
        try {
            List<DocumentPermission> permissions = documentService.getDocumentPermissions(id);
            return ApiResponse.success(permissions);
        } catch (Exception e) {
            log.error("Failed to get document permissions", e);
            return ApiResponse.error(e.getMessage());
        }
    }

    @GetMapping("/user/{userId}/permissions")
    public ApiResponse<List<DocumentPermission>> getUserPermissions(@PathVariable String userId) {
        try {
            List<DocumentPermission> permissions = documentService.getUserPermissions(userId);
            return ApiResponse.success(permissions);
        } catch (Exception e) {
            log.error("Failed to get user permissions", e);
            return ApiResponse.error(e.getMessage());
        }
    }
}
