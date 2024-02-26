package com.ikun.knowledge_back.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ikun.knowledge_back.common.R;
import com.ikun.knowledge_back.entity.Article;

import javax.servlet.http.HttpServletRequest;

public interface ArticleService extends IService<Article> {

    R<Page> getCheckArticle(int i, int i1, HttpServletRequest request);
}
