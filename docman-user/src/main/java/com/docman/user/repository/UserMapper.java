package com.docman.user.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.docman.user.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * User mapper
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
