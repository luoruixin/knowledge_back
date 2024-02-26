package com.ikun.knowledge_back.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ikun.knowledge_back.common.R;
import com.ikun.knowledge_back.dto.CommentDTO;
import com.ikun.knowledge_back.entity.ArticleComment;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface ArticleCommentService extends IService<ArticleComment> {
    R<String> publishComment(CommentDTO commentDTO, HttpServletRequest request);

    R<List<ArticleComment>> getAllComment(HttpServletRequest request);

    R<List<CommentDTO>> getComment(long articleId);
}
