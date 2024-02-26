package com.ikun.knowledge_back.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ikun.knowledge_back.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
