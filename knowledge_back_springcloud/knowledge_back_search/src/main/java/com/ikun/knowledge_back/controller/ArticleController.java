package com.ikun.knowledge_back.controller;

import com.ikun.knowledge_back.common.CustomException;
import com.ikun.knowledge_back.common.R;
import com.ikun.knowledge_back.dto.SearchFromDTO;
import com.ikun.knowledge_back.dto.SearchResult;
import com.ikun.knowledge_back.service.ArticleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/search/article")
public class ArticleController {
    @Autowired
    private ArticleService articleService;

    @Value("${knowledge_back.articleFolderPath}")
    private String articleFolderPath;

    //根据关键词搜索
    @GetMapping("/searchByKeyword")
    public R<SearchResult> searchByKeyword(@RequestBody SearchFromDTO SearchFromDTO,HttpServletRequest request){
        return articleService.searchByKeyword(SearchFromDTO,request);
    }

    //联想功能
    @GetMapping("/suggestion")
    public R<List<String>> getSuggestions(@RequestParam("key") String prefix,HttpServletRequest request){
        try {
            return articleService.getSuggestions(prefix,request);
        }catch (Exception e){
            e.printStackTrace();
            throw new CustomException("联想功能出现未知错误");
        }
    }

    //================================下面这两个为Feign设计的============
    @PostMapping("/insertOrUpdateES")
    private Boolean insertOrUpdateES(@RequestParam("articleDocJson") String articleDocJson){
        return articleService.insertESById(articleDocJson);
    }

    @DeleteMapping("/deleteES")
    private Boolean deleteES(@RequestParam("articleId") Long articleId){
        return articleService.deleteESById(articleId);
    }

}
