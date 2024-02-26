package com.ikun.knowledge_back.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ikun.knowledge_back.dto.ArticleCheckDTO;
import com.ikun.knowledge_back.entity.Article;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ArticleMapper extends BaseMapper<Article> {
    @Select("select article.*,u.name from article inner join user u on article.user_id = u.user_id where article.article_state='待审核' limit #{page},#{pageSize}")
    public List<ArticleCheckDTO> pageCheckArticle(int page, int pageSize);

}
