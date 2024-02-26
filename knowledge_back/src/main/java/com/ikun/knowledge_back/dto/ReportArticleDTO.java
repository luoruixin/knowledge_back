package com.ikun.knowledge_back.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class ReportArticleDTO implements Serializable {
    private Long articleId;
    private String articleContent;
    private String reportClass;
}
