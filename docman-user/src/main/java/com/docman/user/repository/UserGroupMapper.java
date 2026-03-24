package com.docman.user.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.docman.user.entity.UserGroup;
import org.apache.ibatis.annotations.Mapper;

/**
 * User group mapper
 */
@Mapper
public interface UserGroupMapper extends BaseMapper<UserGroup> {
}
