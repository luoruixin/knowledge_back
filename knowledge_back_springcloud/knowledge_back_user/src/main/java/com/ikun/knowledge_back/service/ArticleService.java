package com.ikun.knowledge_back.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ikun.knowledge_back.common.R;
import com.ikun.knowledge_back.doc.ArticleDoc;
import com.ikun.knowledge_back.dto.ArticleDetailDTO;
import com.ikun.knowledge_back.dto.PublishArticleDTO;
import com.ikun.knowledge_back.entity.Article;

import javax.servlet.http.HttpServletRequest;

public interface ArticleService extends IService<Article> {
    ArticleDoc publishArticle(PublishArticleDTO publishArticleDTO, HttpServletRequest request);

    Boolean deleteArticleById(Long id, HttpServletRequest request);

    R<Page> getMyArticle(int page, int pageSize, String condition, HttpServletRequest request);

    R<ArticleDetailDTO> getDetails(Long articleId, HttpServletRequest request);
}
