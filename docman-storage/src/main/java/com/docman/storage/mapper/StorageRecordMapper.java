package com.docman.storage.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.docman.storage.entity.StorageRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * Storage Record Mapper
 */
@Mapper
public interface StorageRecordMapper extends BaseMapper<StorageRecord> {
}
