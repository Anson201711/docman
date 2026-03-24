package com.docman.approval.service;

import com.docman.approval.entity.Approval;

import java.util.List;

public interface ApprovalService {
    Approval createTemplate(Approval approval);
    Approval updateTemplate(Approval approval);
    Approval getTemplate(Long id);
    List<Approval> listTemplates();
    void deleteTemplate(Long id);

    Approval createInstance(Approval approval);
    Approval approve(Long id, Long approverId);
    Approval reject(Long id, Long approverId, String reason);
    Approval rejectBack(Long id, Long approverId, String reason);
    Approval getInstance(Long id);
    List<Approval> listInstances();
    List<Approval> myApprovals(Long userId);
}
