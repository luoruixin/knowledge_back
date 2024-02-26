package com.ikun.knowledge_back.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

//发布文章的DTO
@Data
public class PublishArticleDTO {
    private Long articleId;
    private String title;
    private String articleClass;
    private String articleContent;
    private List<String> articleTag;
    private List<Long> relationArticleIds;
    private String articleState;
}
