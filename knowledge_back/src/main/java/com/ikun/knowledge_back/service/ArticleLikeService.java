package com.ikun.knowledge_back.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ikun.knowledge_back.common.R;
import com.ikun.knowledge_back.entity.Article;
import com.ikun.knowledge_back.entity.ArticleLike;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface ArticleLikeService extends IService<ArticleLike> {
    R<String> like(Long articleId, HttpServletRequest request);

    R<List<Article>> getAllLike(HttpServletRequest request);
}
