package com.docman.document.controller;

import com.docman.document.dto.*;
import com.docman.document.entity.Folder;
import com.docman.document.service.FolderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/folders")
@RequiredArgsConstructor
public class FolderController {

    private final FolderService folderService;

    @GetMapping("/{id}")
    public ApiResponse<FolderContentResponse> getFolderContent(@PathVariable Long id) {
        try {
            FolderContentResponse content = folderService.getFolderContent(id);
            return ApiResponse.success(content);
        } catch (Exception e) {
            log.error("Failed to get folder content", e);
            return ApiResponse.error(e.getMessage());
        }
    }

    @GetMapping("/{id}/detail")
    public ApiResponse<Folder> getFolder(@PathVariable Long id) {
        try {
            Folder folder = folderService.getFolderById(id);
            return ApiResponse.success(folder);
        } catch (Exception e) {
            log.error("Failed to get folder", e);
            return ApiResponse.error(e.getMessage());
        }
    }

    @GetMapping("/root")
    public ApiResponse<List<Folder>> getRootFolders(@RequestParam String ownerId) {
        try {
            List<Folder> folders = folderService.getRootFolders(ownerId);
            return ApiResponse.success(folders);
        } catch (Exception e) {
            log.error("Failed to get root folders", e);
            return ApiResponse.error(e.getMessage());
        }
    }

    @GetMapping("/{id}/subfolders")
    public ApiResponse<List<Folder>> getSubFolders(@PathVariable Long id) {
        try {
            List<Folder> folders = folderService.getSubFolders(id);
            return ApiResponse.success(folders);
        } catch (Exception e) {
            log.error("Failed to get sub folders", e);
            return ApiResponse.error(e.getMessage());
        }
    }

    @PostMapping
    public ApiResponse<Folder> createFolder(@RequestBody FolderCreateRequest request) {
        try {
            Folder folder = folderService.createFolder(request);
            return ApiResponse.success("Folder created successfully", folder);
        } catch (Exception e) {
            log.error("Failed to create folder", e);
            return ApiResponse.error(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ApiResponse<Folder> updateFolder(
            @PathVariable Long id,
            @RequestBody FolderUpdateRequest request) {
        try {
            Folder folder = folderService.updateFolder(id, request);
            return ApiResponse.success("Folder updated successfully", folder);
        } catch (Exception e) {
            log.error("Failed to update folder", e);
            return ApiResponse.error(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteFolder(@PathVariable Long id) {
        try {
            folderService.deleteFolder(id);
            return ApiResponse.success("Folder deleted successfully", null);
        } catch (Exception e) {
            log.error("Failed to delete folder", e);
            return ApiResponse.error(e.getMessage());
        }
    }

    @GetMapping("/{id}/path")
    public ApiResponse<String> getFolderPath(@PathVariable Long id) {
        try {
            String path = folderService.buildFolderPath(id);
            return ApiResponse.success(path);
        } catch (Exception e) {
            log.error("Failed to get folder path", e);
            return ApiResponse.error(e.getMessage());
        }
    }

    @GetMapping("/{id}/parents")
    public ApiResponse<List<Folder>> getParentFolders(@PathVariable Long id) {
        try {
            List<Folder> parents = folderService.getAllParentFolders(id);
            return ApiResponse.success(parents);
        } catch (Exception e) {
            log.error("Failed to get parent folders", e);
            return ApiResponse.error(e.getMessage());
        }
    }
}
