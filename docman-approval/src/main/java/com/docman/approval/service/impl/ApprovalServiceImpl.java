package com.docman.approval.service.impl;

import com.docman.approval.entity.Approval;
import com.docman.approval.service.ApprovalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ApprovalServiceImpl implements ApprovalService {

    private final Map<Long, Approval> templateStore = new ConcurrentHashMap<>();
    private final Map<Long, Approval> instanceStore = new ConcurrentHashMap<>();
    private final AtomicLong templateIdCounter = new AtomicLong(1);
    private final AtomicLong instanceIdCounter = new AtomicLong(1);

    @Override
    public Approval createTemplate(Approval approval) {
        approval.setId(templateIdCounter.getAndIncrement());
        approval.setType("TEMPLATE");
        approval.setStatus("ACTIVE");
        approval.setCreateTime(LocalDateTime.now());
        approval.setUpdateTime(LocalDateTime.now());
        templateStore.put(approval.getId(), approval);
        return approval;
    }

    @Override
    public Approval updateTemplate(Approval approval) {
        approval.setUpdateTime(LocalDateTime.now());
        templateStore.put(approval.getId(), approval);
        return approval;
    }

    @Override
    public Approval getTemplate(Long id) {
        return templateStore.get(id);
    }

    @Override
    public List<Approval> listTemplates() {
        return templateStore.values().stream()
                .filter(a -> "TEMPLATE".equals(a.getType()))
                .collect(Collectors.toList());
    }

    @Override
    public void deleteTemplate(Long id) {
        templateStore.remove(id);
    }

    @Override
    public Approval createInstance(Approval approval) {
        approval.setId(instanceIdCounter.getAndIncrement());
        approval.setType("INSTANCE");
        approval.setStatus("PENDING");
        approval.setCreateTime(LocalDateTime.now());
        approval.setUpdateTime(LocalDateTime.now());
        instanceStore.put(approval.getId(), approval);
        return approval;
    }

    @Override
    public Approval approve(Long id, Long approverId) {
        Approval instance = instanceStore.get(id);
        if (instance == null) {
            throw new RuntimeException("Approval instance not found");
        }
        instance.setApproverId(approverId);
        instance.setStatus("APPROVED");
        instance.setUpdateTime(LocalDateTime.now());
        return instance;
    }

    @Override
    public Approval reject(Long id, Long approverId, String reason) {
        Approval instance = instanceStore.get(id);
        if (instance == null) {
            throw new RuntimeException("Approval instance not found");
        }
        instance.setApproverId(approverId);
        instance.setStatus("REJECTED");
        instance.setComment(reason);
        instance.setUpdateTime(LocalDateTime.now());
        return instance;
    }

    @Override
    public Approval rejectBack(Long id, Long approverId, String reason) {
        Approval instance = instanceStore.get(id);
        if (instance == null) {
            throw new RuntimeException("Approval instance not found");
        }
        instance.setApproverId(approverId);
        instance.setStatus("REJECTED_BACK");
        instance.setComment(reason);
        instance.setUpdateTime(LocalDateTime.now());
        return instance;
    }

    @Override
    public Approval getInstance(Long id) {
        return instanceStore.get(id);
    }

    @Override
    public List<Approval> listInstances() {
        return new ArrayList<>(instanceStore.values());
    }

    @Override
    public List<Approval> myApprovals(Long userId) {
        return instanceStore.values().stream()
                .filter(a -> userId.equals(a.getApproverId()))
                .collect(Collectors.toList());
    }
}
