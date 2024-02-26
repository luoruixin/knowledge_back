package com.ikun.knowledge_back.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ikun.knowledge_back.entity.Article;
import com.ikun.knowledge_back.entity.ArticleCollect;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ArticleCollectMapper extends BaseMapper<ArticleCollect> {
    @Select("select * from article where article.article_id=(select article_id from article_collect where collection_id=#{collectionId})")
    public List<Article> getCollectArticle(Long collectionId);
}
