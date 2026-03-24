package com.docman.classification.service;

import com.docman.classification.entity.Classification;

import java.util.List;

public interface ClassificationService {

    Classification create(Classification classification);

    Classification update(Classification classification);

    void delete(String id);

    Classification getById(String id);

    List<Classification> getTree();

    List<Classification> getChildren(String parentId);

    List<Classification> search(String keyword);
}
