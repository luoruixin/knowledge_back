package com.ikun.knowledge_back.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ikun.knowledge_back.entity.Article;
import com.ikun.knowledge_back.entity.ArticleTag;
import com.ikun.knowledge_back.mapper.ArticleMapper;
import com.ikun.knowledge_back.mapper.ArticleTagMapper;
import com.ikun.knowledge_back.service.ArticleService;
import com.ikun.knowledge_back.service.ArticleTagService;
import org.springframework.stereotype.Service;

@Service
public class ArticleTagServiceImpl extends ServiceImpl<ArticleTagMapper, ArticleTag> implements ArticleTagService {
}
