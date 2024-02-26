package com.ikun.knowledge_back.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ikun.knowledge_back.dto.ArticleWithCommentDTO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ArticleWithCommentMapper extends BaseMapper<ArticleWithCommentDTO> {
}
