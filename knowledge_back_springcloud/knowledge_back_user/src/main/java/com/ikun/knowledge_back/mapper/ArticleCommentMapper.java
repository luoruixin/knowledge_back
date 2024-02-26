package com.ikun.knowledge_back.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ikun.knowledge_back.dto.CommentDTO;
import com.ikun.knowledge_back.entity.ArticleComment;
import com.ikun.knowledge_back.service.ArticleCommentService;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ArticleCommentMapper extends BaseMapper<ArticleComment> {
    @Select("select article_comment.*,name as user_name from article_comment inner join user on article_comment.user_id = user.user_id where article_id=#{articleId} and article_comment.comment_state='发布'")
    public List<CommentDTO> getCommentDTO(Long articleId);
}
