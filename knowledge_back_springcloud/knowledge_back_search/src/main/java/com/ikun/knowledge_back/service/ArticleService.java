package com.ikun.knowledge_back.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ikun.knowledge_back.common.R;
import com.ikun.knowledge_back.dto.SearchFromDTO;
import com.ikun.knowledge_back.dto.SearchResult;
import com.ikun.knowledge_back.entity.Article;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;

public interface ArticleService extends IService<Article> {
    R<SearchResult> searchByKeyword(SearchFromDTO SearchFromDTO, HttpServletRequest request);

    R<List<String>> getSuggestions(String prefix, HttpServletRequest request) throws IOException;

    Boolean insertESById(String articleDocJson);

    Boolean deleteESById(Long articleId);
}
