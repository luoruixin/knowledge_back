package com.ikun.knowledge_back.controller;

import com.ikun.knowledge_back.service.ArticleCommentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/admin/articleComment")
public class ArticleCommentController {
    @Autowired
    private ArticleCommentService articleCommentService;

    //查看被举报的评论

    //管理端删除评论（认为举报合理）
}
