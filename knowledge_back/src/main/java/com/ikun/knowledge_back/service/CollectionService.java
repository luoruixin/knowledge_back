package com.ikun.knowledge_back.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ikun.knowledge_back.common.R;
import com.ikun.knowledge_back.dto.ArticleCollectDTO;
import com.ikun.knowledge_back.entity.Article;
import com.ikun.knowledge_back.entity.Collection;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface CollectionService extends IService<Collection> {
    R<String> create(String collectionName, HttpServletRequest request);

    R<List<Collection>> getAllCollection(HttpServletRequest request);

    R<String> collectArticle(ArticleCollectDTO articleCollectDTO, HttpServletRequest request);

    R<List<Article>> getCollectArticle(Long collectionId, HttpServletRequest request);
}
