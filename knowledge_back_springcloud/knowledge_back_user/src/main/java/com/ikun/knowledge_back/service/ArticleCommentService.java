package com.ikun.knowledge_back.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ikun.knowledge_back.common.R;
import com.ikun.knowledge_back.dto.CommentDTO;
import com.ikun.knowledge_back.entity.ArticleComment;

import javax.servlet.http.HttpServletRequest;

public interface ArticleCommentService extends IService<ArticleComment> {
    R<String> publishComment(CommentDTO commentDTO, HttpServletRequest request);
}
