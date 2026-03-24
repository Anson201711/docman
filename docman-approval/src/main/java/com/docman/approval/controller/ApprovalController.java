package com.docman.approval.controller;

import com.docman.approval.entity.Approval;
import com.docman.approval.service.ApprovalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/approval")
@RequiredArgsConstructor
public class ApprovalController {

    private final ApprovalService approvalService;

    @PostMapping("/template")
    public ResponseEntity<Approval> createTemplate(@RequestBody Approval approval) {
        return ResponseEntity.ok(approvalService.createTemplate(approval));
    }

    @PutMapping("/template/{id}")
    public ResponseEntity<Approval> updateTemplate(@PathVariable Long id, @RequestBody Approval approval) {
        approval.setId(id);
        return ResponseEntity.ok(approvalService.updateTemplate(approval));
    }

    @GetMapping("/template/{id}")
    public ResponseEntity<Approval> getTemplate(@PathVariable Long id) {
        return ResponseEntity.ok(approvalService.getTemplate(id));
    }

    @GetMapping("/template/list")
    public ResponseEntity<List<Approval>> listTemplates() {
        return ResponseEntity.ok(approvalService.listTemplates());
    }

    @DeleteMapping("/template/{id}")
    public ResponseEntity<Void> deleteTemplate(@PathVariable Long id) {
        approvalService.deleteTemplate(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/instance")
    public ResponseEntity<Approval> createInstance(@RequestBody Approval approval) {
        return ResponseEntity.ok(approvalService.createInstance(approval));
    }

    @PutMapping("/instance/{id}/approve")
    public ResponseEntity<Approval> approve(@PathVariable Long id, @RequestParam Long approverId) {
        return ResponseEntity.ok(approvalService.approve(id, approverId));
    }

    @PutMapping("/instance/{id}/reject")
    public ResponseEntity<Approval> reject(@PathVariable Long id, @RequestParam Long approverId, @RequestParam String reason) {
        return ResponseEntity.ok(approvalService.reject(id, approverId, reason));
    }

    @PutMapping("/instance/{id}/reject-back")
    public ResponseEntity<Approval> rejectBack(@PathVariable Long id, @RequestParam Long approverId, @RequestParam String reason) {
        return ResponseEntity.ok(approvalService.rejectBack(id, approverId, reason));
    }

    @GetMapping("/instance/{id}")
    public ResponseEntity<Approval> getInstance(@PathVariable Long id) {
        return ResponseEntity.ok(approvalService.getInstance(id));
    }

    @GetMapping("/instance/list")
    public ResponseEntity<List<Approval>> listInstances() {
        return ResponseEntity.ok(approvalService.listInstances());
    }

    @GetMapping("/instance/my")
    public ResponseEntity<List<Approval>> myApprovals(@RequestParam Long userId) {
        return ResponseEntity.ok(approvalService.myApprovals(userId));
    }
}
