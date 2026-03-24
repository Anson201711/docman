package com.docman.document.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.docman.document.entity.TrashRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TrashRecordRepository extends BaseMapper<TrashRecord> {

    List<TrashRecord> findByOwnerId(@Param("ownerId") String ownerId);

    TrashRecord findByDocumentId(@Param("documentId") Long documentId);

    List<TrashRecord> findExpiredRecords();

    void deleteByOwnerId(@Param("ownerId") String ownerId);

    void deleteByDocumentId(@Param("documentId") Long documentId);
}
