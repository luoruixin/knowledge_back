package com.ikun.knowledge_back.dto;

import com.ikun.knowledge_back.doc.ArticleDoc;
import lombok.Data;

import java.util.List;

//用于es的分页搜索
@Data
public class SearchResult {
    private Long total;
    private List<ArticleDoc> articles;

    public SearchResult() {
    }

    public SearchResult(long total, List<ArticleDoc> articles) {
        this.articles=articles;
        this.total=total;
    }
}
