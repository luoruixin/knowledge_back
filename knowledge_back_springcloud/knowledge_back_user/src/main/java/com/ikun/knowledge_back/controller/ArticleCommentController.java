package com.ikun.knowledge_back.controller;

import com.ikun.knowledge_back.common.R;
import com.ikun.knowledge_back.dto.CommentDTO;
import com.ikun.knowledge_back.service.ArticleCommentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@RestController
@RequestMapping("/user0/articleComment")
public class ArticleCommentController {
    @Autowired
    private ArticleCommentService articleCommentService;
    //发表评论(评论数+1)
    @PostMapping("/publishComment")
    public R<String> publishComment(@RequestBody CommentDTO commentDTO, HttpServletRequest request){
        return articleCommentService.publishComment(commentDTO,request);//getMyArticle是查看自己的所有文章（发布、草稿...）
    }

    //查看我的所有评论

    //查看别人对我的文章的所有评论

    //删除评论

    //举报评论

    //查看被举报的评论

}
