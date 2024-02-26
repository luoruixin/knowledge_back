package com.ikun.knowledge_back.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class ArticleCollectDTO implements Serializable {
    private Long articleId;
    private List<Long> collectionIds;
}
