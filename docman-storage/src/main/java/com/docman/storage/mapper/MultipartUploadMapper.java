package com.docman.storage.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.docman.storage.entity.MultipartUpload;
import org.apache.ibatis.annotations.Mapper;

/**
 * Multipart Upload Mapper
 */
@Mapper
public interface MultipartUploadMapper extends BaseMapper<MultipartUpload> {
}
