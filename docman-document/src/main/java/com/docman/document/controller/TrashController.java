package com.docman.document.controller;

import com.docman.document.dto.ApiResponse;
import com.docman.document.entity.TrashRecord;
import com.docman.document.service.TrashService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/trash")
@RequiredArgsConstructor
public class TrashController {

    private final TrashService trashService;

    @GetMapping
    public ApiResponse<List<TrashRecord>> getTrashList(@RequestParam String ownerId) {
        try {
            List<TrashRecord> records = trashService.getTrashList(ownerId);
            return ApiResponse.success(records);
        } catch (Exception e) {
            log.error("Failed to get trash list", e);
            return ApiResponse.error(e.getMessage());
        }
    }

    @PostMapping("/{documentId}/restore")
    public ApiResponse<TrashRecord> restoreDocument(@PathVariable Long documentId) {
        try {
            TrashRecord record = trashService.restoreFromTrash(documentId);
            return ApiResponse.success("Document restored successfully", record);
        } catch (Exception e) {
            log.error("Failed to restore document", e);
            return ApiResponse.error(e.getMessage());
        }
    }

    @DeleteMapping("/{documentId}/permanent")
    public ApiResponse<Void> permanentDelete(@PathVariable Long documentId) {
        try {
            trashService.permanentDelete(documentId);
            return ApiResponse.success("Document permanently deleted", null);
        } catch (Exception e) {
            log.error("Failed to permanently delete document", e);
            return ApiResponse.error(e.getMessage());
        }
    }

    @DeleteMapping("/empty")
    public ApiResponse<Void> emptyTrash(@RequestParam String ownerId) {
        try {
            trashService.emptyTrash(ownerId);
            return ApiResponse.success("Trash emptied successfully", null);
        } catch (Exception e) {
            log.error("Failed to empty trash", e);
            return ApiResponse.error(e.getMessage());
        }
    }
}
