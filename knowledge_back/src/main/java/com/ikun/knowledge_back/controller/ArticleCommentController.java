package com.ikun.knowledge_back.controller;

import com.ikun.knowledge_back.common.R;
import com.ikun.knowledge_back.dto.CommentDTO;
import com.ikun.knowledge_back.entity.ArticleComment;
import com.ikun.knowledge_back.service.ArticleCommentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/articleComment")
public class ArticleCommentController {
    @Autowired
    private ArticleCommentService articleCommentService;
    //发表评论(评论数+1)
    @PostMapping("/publishComment")
    public R<String> publishComment(@RequestBody CommentDTO commentDTO, HttpServletRequest request){
        return articleCommentService.publishComment(commentDTO,request);//getMyArticle是查看自己的所有文章（发布、草稿...）
    }

    //查看我的所有评论
    @GetMapping("/getAllComment")
    public R<List<ArticleComment>> getAllComment(HttpServletRequest request){
        return articleCommentService.getAllComment(request);//getMyArticle是查看自己的所有文章（发布、草稿...）
    }

    //查看别人对我的文章的所有评论
@GetMapping("/getAllMyArticleComments")
    public R<List<CommentDTO>> getAllMyArticleComments(@RequestParam("articleId") long articleId){
        return articleCommentService.getComment(articleId);
}
    //删除评论

    //举报评论

    //查看被举报的评论

    //管理端删除评论（认为举报合理）
}
