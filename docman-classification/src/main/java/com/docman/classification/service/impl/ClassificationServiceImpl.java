package com.docman.classification.service.impl;

import com.docman.classification.entity.Classification;
import com.docman.classification.service.ClassificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClassificationServiceImpl implements ClassificationService {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String CACHE_KEY = "classification:tree";
    private static final String ITEM_KEY = "classification:item:";

    @Override
    public Classification create(Classification classification) {
        classification.setId(UUID.randomUUID().toString());
        classification.setCreatedAt(LocalDateTime.now());
        classification.setUpdatedAt(LocalDateTime.now());
        classification.setStatus("ACTIVE");

        redisTemplate.opsForValue().set(ITEM_KEY + classification.getId(), classification);
        clearTreeCache();

        log.info("Created classification: {}", classification.getId());
        return classification;
    }

    @Override
    public Classification update(Classification classification) {
        classification.setUpdatedAt(LocalDateTime.now());
        redisTemplate.opsForValue().set(ITEM_KEY + classification.getId(), classification);
        clearTreeCache();

        log.info("Updated classification: {}", classification.getId());
        return classification;
    }

    @Override
    public void delete(String id) {
        Classification classification = getById(id);
        if (classification != null) {
            classification.setStatus("DELETED");
            classification.setUpdatedAt(LocalDateTime.now());
            redisTemplate.opsForValue().set(ITEM_KEY + id, classification);
            clearTreeCache();
        }
        log.info("Deleted classification: {}", id);
    }

    @Override
    public Classification getById(String id) {
        Object cached = redisTemplate.opsForValue().get(ITEM_KEY + id);
        if (cached != null) {
            return (Classification) cached;
        }
        return null;
    }

    @Override
    public List<Classification> getTree() {
        Object cached = redisTemplate.opsForValue().get(CACHE_KEY);
        if (cached != null) {
            return (List<Classification>) cached;
        }

        List<Classification> allClassifications = new ArrayList<>();
        List<Classification> tree = buildTree(allClassifications, null);

        redisTemplate.opsForValue().set(CACHE_KEY, tree);
        return tree;
    }

    @Override
    public List<Classification> getChildren(String parentId) {
        Object cached = redisTemplate.opsForValue().get(CACHE_KEY);
        if (cached != null) {
            List<Classification> all = (List<Classification>) cached;
            return all.stream()
                    .filter(c -> parentId.equals(c.getParentId()))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    @Override
    public List<Classification> search(String keyword) {
        Object cached = redisTemplate.opsForValue().get(CACHE_KEY);
        if (cached != null) {
            List<Classification> all = (List<Classification>) cached;
            return all.stream()
                    .filter(c -> c.getName().contains(keyword) ||
                                 (c.getCode() != null && c.getCode().contains(keyword)))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    private List<Classification> buildTree(List<Classification> all, String parentId) {
        return all.stream()
                .filter(c -> Objects.equals(parentId, c.getParentId()))
                .collect(Collectors.toList());
    }

    private void clearTreeCache() {
        redisTemplate.delete(CACHE_KEY);
    }
}
