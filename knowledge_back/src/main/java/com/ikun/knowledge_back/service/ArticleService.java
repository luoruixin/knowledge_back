package com.ikun.knowledge_back.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ikun.knowledge_back.common.R;
import com.ikun.knowledge_back.dto.*;
import com.ikun.knowledge_back.entity.Article;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;

public interface ArticleService extends IService<Article> {
    R<SearchResult> searchByKeyword(SearchFromDTO SearchFromDTO, HttpServletRequest request);

    R<List<String>> getSuggestions(String prefix, HttpServletRequest request) throws IOException;

    R<String> publishArticle(PublishArticleDTO publishArticleDTO, HttpServletRequest request);

    R<String> deleteArticleById(Long id, HttpServletRequest request);

    R<Page> getMyArticle(int page, int pageSize, String condition, HttpServletRequest request);

    R<ArticleDetailDTO> getDetails(Long articleId, String condition, HttpServletRequest request);

    R<List<ArticleWithCommentDTO>>getArticleWithComment(HttpServletRequest request);

    R<String> editArticle(PublishArticleDTO publishArticleDTO, HttpServletRequest request);

    R<Page> getCheckArticle(int page, int pageSize, HttpServletRequest request);

    R<String> changeArticleState(Long articleId,String articleState, HttpServletRequest request);

    R<String> checkArticle(Long articleId, String articleState, HttpServletRequest request);

    R<String> changeReportArticleState(Long articleId, String targetState);

    R<List<String>> getTags(String title,String content);
}
