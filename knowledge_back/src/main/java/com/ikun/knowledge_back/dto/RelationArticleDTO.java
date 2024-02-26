package com.ikun.knowledge_back.dto;

import lombok.Data;

// TODO: RalationArticleDTO
@Data
public class RelationArticleDTO {
    private Long relationArticleId;
    private Long relatedArticleId;
    private String relatedArticleTitle;
}
