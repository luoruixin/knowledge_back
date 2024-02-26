package com.ikun.knowledge_back.controller;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ikun.knowledge_back.common.R;
import com.ikun.knowledge_back.entity.Article;
import com.ikun.knowledge_back.entity.ArticleLike;
import com.ikun.knowledge_back.service.ArticleLikeService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/user0/articleLike")
public class ArticleLikeController {
    @Autowired
    private ArticleLikeService articleLikeService;

    //为某篇文章点赞
    @PostMapping("/like")
    public R<String> like(Long articleId, HttpServletRequest request){
        return articleLikeService.like(articleId,request);//getMyArticle是查看自己的所有文章（发布、草稿...）
    }

    //查看已点赞的文章
    @GetMapping("/getAllLike")
    public R<List<Article>> getAllLike(HttpServletRequest request){
        return articleLikeService.getAllLike(request);//getMyArticle是查看自己的所有文章（发布、草稿...）
    }

    //删除点赞的文章
}
