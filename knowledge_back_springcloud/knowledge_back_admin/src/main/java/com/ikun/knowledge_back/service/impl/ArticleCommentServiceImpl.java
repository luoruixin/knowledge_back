package com.ikun.knowledge_back.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ikun.knowledge_back.entity.ArticleComment;
import com.ikun.knowledge_back.mapper.ArticleCommentMapper;
import com.ikun.knowledge_back.service.ArticleCommentService;
import org.springframework.stereotype.Service;

@Service
public class ArticleCommentServiceImpl extends ServiceImpl<ArticleCommentMapper, ArticleComment> implements ArticleCommentService {

}
