package com.docman.user.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.docman.user.entity.UserGroupMember;
import org.apache.ibatis.annotations.Mapper;

/**
 * User group member mapper
 */
@Mapper
public interface UserGroupMemberMapper extends BaseMapper<UserGroupMember> {
}
