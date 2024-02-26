package com.ikun.knowledge_back.controller;

import com.ikun.knowledge_back.common.CustomException;
import com.ikun.knowledge_back.common.R;
import com.ikun.knowledge_back.dto.ArticleCollectDTO;
import com.ikun.knowledge_back.entity.Article;
import com.ikun.knowledge_back.entity.Collection;
import com.ikun.knowledge_back.service.CollectionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/user0/collection")
public class CollectionController {
    @Autowired
    private CollectionService collectionService;

    //创建收藏夹
    @PostMapping("/create")
    public R<String> create(String collectionName, HttpServletRequest request){
        return collectionService.create(collectionName,request);
    }

    //查看用户的所有收藏夹
    @GetMapping("/getAllCollection")
    public R<List<Collection>> getAllCollection(HttpServletRequest request){
        return collectionService.getAllCollection(request);
    }

    //收藏文章
    @PostMapping("/collectArticle")
    public R<String> collectArticle(@RequestBody ArticleCollectDTO articleCollectDTO,HttpServletRequest request){
        return collectionService.collectArticle(articleCollectDTO,request);
    }

    //查看某个收藏夹中的文章
    @GetMapping("/getCollectArticle")
    public R<List<Article>> getCollectArticle(Long collectionId,HttpServletRequest request){
        return collectionService.getCollectArticle(collectionId,request);
    }

    //修改收藏夹

    //删除收藏夹

}
