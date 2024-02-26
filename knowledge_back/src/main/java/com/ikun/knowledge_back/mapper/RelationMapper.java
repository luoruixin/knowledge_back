package com.ikun.knowledge_back.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ikun.knowledge_back.entity.Article;
import com.ikun.knowledge_back.entity.Relation;
import com.ikun.knowledge_back.entity.Report;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface RelationMapper extends BaseMapper<Relation> {
    @Select("select * from article where article.article_id in (select article_id from relation where related_article_id=#{articleId})")
    public List<Article> getRelatedArticle(Long articleId);
}
