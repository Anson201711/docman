package com.docman.document.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.docman.document.entity.Folder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FolderRepository extends BaseMapper<Folder> {

    List<Folder> findByParentId(@Param("parentId") Long parentId);

    List<Folder> findByOwnerId(@Param("ownerId") String ownerId);

    List<Folder> findByParentIdAndOwnerId(@Param("parentId") Long parentId, @Param("ownerId") String ownerId);

    Folder findByNameAndParentId(@Param("name") String name, @Param("parentId") Long parentId);

    List<Folder> findDeletedByOwnerId(@Param("ownerId") String ownerId);
}
