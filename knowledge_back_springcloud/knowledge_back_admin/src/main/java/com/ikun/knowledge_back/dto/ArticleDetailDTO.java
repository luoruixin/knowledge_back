package com.ikun.knowledge_back.dto;

import com.ikun.knowledge_back.entity.Article;
import com.ikun.knowledge_back.entity.ArticleTag;
import com.ikun.knowledge_back.entity.User;
import lombok.Data;

import java.util.List;

@Data
public class ArticleDetailDTO {
    private Article article;
    private User author;
    private List<CommentDTO> articleComments;
    private List<RelationArticleDTO> relationArticles;
    private List<ArticleTag> articleTagList;

    private boolean isLike;
    private boolean isCollected;
}
