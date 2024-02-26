package com.ikun.knowledge_back.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ikun.knowledge_back.common.R;
import com.ikun.knowledge_back.service.ArticleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@RestController
@RequestMapping("/admin/article")
public class ArticleController {
    @Autowired
    private ArticleService articleService;

    @Value("${knowledge_back.articleFolderPath}")
    private String articleFolderPath;


    //查看审核中的文章
    @GetMapping("/getCheckArticle")
    R<Page> getCheckArticle(Integer page, Integer pageSize, HttpServletRequest request){
        return articleService.getCheckArticle(1,100,request);
    }
    //管理员查看审核中文章的详情
    @GetMapping("/getCheckDetails")
    public R<ArticleDetailDTO> getCheckDetails(@RequestParam Long articleId,HttpServletRequest request){

        //开始查询
        String condition="待审核";
        return articleService.getDetails(articleId,condition,request);
    }
}
