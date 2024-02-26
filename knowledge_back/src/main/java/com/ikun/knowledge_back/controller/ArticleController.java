package com.ikun.knowledge_back.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.util.concurrent.RateLimiter;
import com.ikun.knowledge_back.common.CustomException;
import com.ikun.knowledge_back.common.R;
import com.ikun.knowledge_back.dto.*;
import com.ikun.knowledge_back.entity.Article;
import com.ikun.knowledge_back.service.ArticleService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/article")
public class ArticleController {
    @Autowired
    private ArticleService articleService;

    @Value("${knowledge_back.articleFolderPath}")
    private String articleFolderPath;

    @Autowired
    private RestHighLevelClient client;  //操纵es的

    //查看自己最近发布的10篇文章
    @GetMapping("/getMyPublish10")
    public R<Page> getMyPublish10(HttpServletRequest request){
        String condition="已发布";
        return articleService.getMyArticle(1,10,condition,request);//getMyArticle是查看自己的所有文章（发布、草稿...）
    }

    //查看自己的所有文章
    @GetMapping("/getMyAllArticle")
    public R<Page> getMyAllArticle(int page,int pageSize,HttpServletRequest request){
        String condition=null;
        return articleService.getMyArticle(page,pageSize,condition, request);//getMyArticle是查看自己的所有文章（发布、草稿...）
    }

    //查看自己发布的所有文章
    @GetMapping("/getMyPublish")
    public R<Page> getMyPublish(int page,int pageSize,HttpServletRequest request){
        String condition="已发布";
        return articleService.getMyArticle(page,pageSize,condition, request);//getMyArticle是查看自己的所有文章（发布、草稿...）
    }

    //查看自己保存的草稿
    @GetMapping("/getMyDraft")
    public R<Page> getMyDraft(int page,int pageSize,HttpServletRequest request){
        String condition="草稿";
        return articleService.getMyArticle(page,pageSize,condition, request);//getMyArticle是查看自己的所有文章（发布、草稿...）
    }

    //查看自己审核中的文章
    @GetMapping("/getMyCheck")
    public R<Page> getMyCheck(int page,int pageSize,HttpServletRequest request){
        String condition="待审核";
        return articleService.getMyArticle(page,pageSize,condition, request);//getMyArticle是查看自己的所有文章（发布、草稿...）
    }

    //查看自己已退回的文章
    @GetMapping("/getMyReturn")
    public R<Page> getMyReturn(int page,int pageSize,HttpServletRequest request){
        String condition="已退回";
        return articleService.getMyArticle(page,pageSize,condition, request);//getMyArticle是查看自己的所有文章（发布、草稿...）
    }

    //根据关键词搜索
    @GetMapping("/searchByKeyword")
    public R<SearchResult> searchByKeyword(SearchFromDTO SearchFromDTO,HttpServletRequest request){
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

    //TODO:重点测试
    //发布文章
    @PostMapping("/publishArticle")
    public R<String> publishArticle(@RequestBody PublishArticleDTO publishArticleDTO,HttpServletRequest request){
        return articleService.publishArticle(publishArticleDTO,request);
    }

    //TODO:重点测试
    //编辑文章
    @PutMapping("/editArticle")
    public R<String> editArticle(@RequestBody PublishArticleDTO publishArticleDTO,HttpServletRequest request){
        return articleService.editArticle(publishArticleDTO,request);
    }



    //删除文章或者草稿、已退回...
    @DeleteMapping("/deleteArticleById")
    public R<String> deleteArticleById(@RequestParam("id") Long id,HttpServletRequest request){
        return articleService.deleteArticleById(id,request);

    }


    // 创建一个每秒生成两个令牌的RateLimiter  (限制该接口的并发数)
    private final RateLimiter rateLimiter = RateLimiter.create(2.0);
    //点击文章查看文章详情
    //TODO: 完善文章详情
    @GetMapping("/getDetails")
    @Transactional
    public R<ArticleDetailDTO> getDetails(@RequestParam Long articleId,HttpServletRequest request){
        // 尝试获取令牌，如果没有可用的令牌则等待
        rateLimiter.acquire();
        //浏览量+1
        Article article = articleService.getById(articleId);
        article.setScanCount(article.getScanCount()+1);
        articleService.updateById(article);

        //更新es的浏览量
        UpdateRequest esRequest = new UpdateRequest("article", articleId.toString());
        //准备参数
        esRequest.doc(
                "scanCount",article.getScanCount()
        );
        //2.发送请求
        try {
            client.update(esRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        //开始查询
        String condition="已发布";
        return articleService.getDetails(articleId,condition,request);
    }

    // 查看草稿详情
    @GetMapping("/getEditDetails")
    public R<ArticleDetailDTO> getEditDetails(@RequestParam Long articleId,HttpServletRequest request){
        // 尝试获取令牌，如果没有可用的令牌则等待
        rateLimiter.acquire();
        //浏览量+1
//        Article article = articleService.getById(articleId);
//        article.setScanCount(article.getScanCount()+1);
//        articleService.updateById(article);

        //开始查询
        String condition="草稿";
        return articleService.getDetails(articleId,condition,request);
    }

    //管理员查看审核中文章的详情
    @GetMapping("/getCheckDetails")
    public R<ArticleDetailDTO> getCheckDetails(@RequestParam Long articleId,HttpServletRequest request){

        //开始查询
        String condition="待审核";
        return articleService.getDetails(articleId,condition,request);
    }

    //返回带有评论的文章
    @GetMapping("/getArticleWithComment")
    R<List<ArticleWithCommentDTO>> getArticleWithComment(HttpServletRequest request){
      return  articleService.getArticleWithComment(request);
    }

    //管理员审核中文章
    @PutMapping("/checkArticle")
    R<String> checkArticle(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        Long articleId = Long.parseLong(params.get("articleId").toString());
        String articleState = params.get("articleState").toString();
        System.out.println("文章状态" + articleState);
        return articleService.checkArticle(articleId, articleState, request);
    }


    //管理员查看审核中的文章
    @GetMapping("/getCheckArticle")
    R<Page> getCheckArticle(Integer page,Integer pageSize,HttpServletRequest request){
        return articleService.getCheckArticle(1,100,request);
    }
    //查看已退回的文章

    @GetMapping("/getTags")
    R<List<String>> getTags(String title,String content){
        return articleService.getTags(title,content);
    }
}
