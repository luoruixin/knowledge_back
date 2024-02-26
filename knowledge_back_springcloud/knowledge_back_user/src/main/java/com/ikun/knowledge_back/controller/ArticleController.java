package com.ikun.knowledge_back.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.util.concurrent.RateLimiter;
import com.ikun.knowledge_back.FeignClient.ESClient;
import com.ikun.knowledge_back.common.CustomException;
import com.ikun.knowledge_back.common.JacksonObjectMapper;
import com.ikun.knowledge_back.common.MqConstants;
import com.ikun.knowledge_back.common.R;
import com.ikun.knowledge_back.doc.ArticleDoc;
import com.ikun.knowledge_back.dto.ArticleDetailDTO;
import com.ikun.knowledge_back.dto.PublishArticleDTO;
import com.ikun.knowledge_back.entity.Article;
import com.ikun.knowledge_back.service.ArticleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Lazy;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/user0/article")
public class ArticleController {
    @Autowired
    private ArticleService articleService;

    @Value("${knowledge_back.articleFolderPath}")
    private String articleFolderPath;

    @Autowired
    @Lazy  //这里必须加上，可以解决循环依赖的问题
    private ESClient esClient;

    //查看自己最近发布的10篇文章
    @GetMapping("/getMyPublish10")
    public R<Page> getMyPublish10(HttpServletRequest request){
        String condition="已发布";
        return articleService.getMyArticle(1,10,condition,request);//getMyArticle是查看自己的所有文章（发布、草稿...）
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

    //TODO:重点测试
    //发布文章
    @PostMapping("/publishArticle")
    @Transactional
    public R<String> publishArticle(@RequestBody PublishArticleDTO publishArticleDTO,HttpServletRequest request){
        ArticleDoc articleDoc = articleService.publishArticle(publishArticleDTO, request);
        if(articleDoc==null){
            throw new RuntimeException("发布成功");
        }
        JacksonObjectMapper jacksonObjectMapper=new JacksonObjectMapper();
        String articleDocJson=null;
        try {
            articleDocJson = jacksonObjectMapper.writeValueAsString(articleDoc);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        Boolean aBoolean = esClient.insertOrUpdateES(articleDocJson);
        if(!aBoolean){
//            return R.error("发布失败");    //这里不会回滚，因为要抛出异常spring才会回滚事务
            throw new RuntimeException("发布失败");
        }
        return R.success("发布成功");
    }

    //删除文章或者草稿、已退回...
    @DeleteMapping("/deleteArticleById")
    @Transactional
    public R<String> deleteArticleById(@RequestParam("id") Long id,HttpServletRequest request){
        Boolean aBoolean1 = articleService.deleteArticleById(id, request);
        if(!aBoolean1){
//            return R.error("发布失败");    //这里不会回滚，因为要抛出异常spring才会回滚事务
            throw new RuntimeException("删除失败");
        }
        //删除ES中的数据
        Boolean aBoolean = esClient.deleteES(id);
        if(!aBoolean){
//            return R.error("发布失败");    //这里不会回滚，因为要抛出异常spring才会回滚事务
            throw new RuntimeException("删除失败");
        }
        return R.success("删除成功");
    }

    // 创建一个每秒生成两个令牌的RateLimiter  (限制该接口的并发数)
    private final RateLimiter rateLimiter = RateLimiter.create(2.0);
    //点击文章查看文章详情
    //TODO:该方法还没完善
    @GetMapping("/getDetails")
    public R<ArticleDetailDTO> getDetails(@RequestParam Long articleId,HttpServletRequest request){
        // 尝试获取令牌，如果没有可用的令牌则等待
        rateLimiter.acquire();
        //浏览量+1
        Article article = articleService.getById(articleId);
        article.setScanCount(article.getScanCount()+1);
        articleService.updateById(article);

        //开始查询
        return articleService.getDetails(articleId,request);
    }
}
