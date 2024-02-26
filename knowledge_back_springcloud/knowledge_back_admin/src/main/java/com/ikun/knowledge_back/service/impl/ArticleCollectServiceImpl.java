package com.ikun.knowledge_back.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ikun.knowledge_back.entity.ArticleCollect;
import com.ikun.knowledge_back.mapper.ArticleCollectMapper;
import com.ikun.knowledge_back.service.ArticleCollectService;
import org.springframework.stereotype.Service;

@Service
public class ArticleCollectServiceImpl extends ServiceImpl<ArticleCollectMapper, ArticleCollect> implements ArticleCollectService {
}
