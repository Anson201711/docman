package com.docman.document.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.docman.document.entity.Document;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DocumentRepository extends BaseMapper<Document> {

    List<Document> findByFolderId(@Param("folderId") Long folderId);

    List<Document> findByOwnerId(@Param("ownerId") String ownerId);

    List<Document> findByFolderIdAndOwnerId(@Param("folderId") Long folderId, @Param("ownerId") String ownerId);

    Document findByNameAndFolderId(@Param("name") String name, @Param("folderId") Long folderId);

    List<Document> findDeletedByOwnerId(@Param("ownerId") String ownerId);
}
