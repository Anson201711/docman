package com.docman.document.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.docman.document.entity.DocumentPermission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DocumentPermissionRepository extends BaseMapper<DocumentPermission> {

    List<DocumentPermission> findByDocumentId(@Param("documentId") Long documentId);

    List<DocumentPermission> findByUserId(@Param("userId") String userId);

    DocumentPermission findByDocumentIdAndUserId(@Param("documentId") Long documentId, @Param("userId") String userId);

    void deleteByDocumentId(@Param("documentId") Long documentId);
}
