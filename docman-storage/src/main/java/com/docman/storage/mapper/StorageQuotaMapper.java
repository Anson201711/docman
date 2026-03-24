package com.docman.storage.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.docman.storage.entity.StorageQuota;
import org.apache.ibatis.annotations.Mapper;

/**
 * Storage Quota Mapper
 */
@Mapper
public interface StorageQuotaMapper extends BaseMapper<StorageQuota> {
}
