package com.ikun.knowledge_back.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ikun.knowledge_back.common.R;
import com.ikun.knowledge_back.common.UserHolder;
import com.ikun.knowledge_back.entity.Article;
import com.ikun.knowledge_back.entity.ArticleLike;
import com.ikun.knowledge_back.mapper.ArticleLikeMapper;
import com.ikun.knowledge_back.service.ArticleLikeService;
import com.ikun.knowledge_back.service.ArticleService;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ArticleLikeServiceImpl extends ServiceImpl<ArticleLikeMapper, ArticleLike> implements ArticleLikeService {
    @Autowired
    private ArticleService articleService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private RestHighLevelClient client;  //操纵es的
    @Transactional
    @Override
    public R<String> like(Long articleId, HttpServletRequest request) {
        Long currentId = UserHolder.getCurrentId(request,stringRedisTemplate);
        if(articleId==null){
            return R.error("文章id为空");
        }
        ArticleLike articleLike=new ArticleLike();

        LambdaQueryWrapper<ArticleLike> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(ArticleLike::getArticleId,articleId).eq(ArticleLike::getUserId,currentId);
        ArticleLike articleLikeOne = getOne(queryWrapper);
        //查询是否已经点赞，如果已经点赞就取消点赞
        Article article = articleService.getById(articleId);
        if(articleLikeOne!=null){
            removeById(articleLikeOne.getArticleLikeId());

            //点赞数-1
            article.setLikeCount(article.getLikeCount()-1);
            articleService.updateById(article);


        }else {
            //如果没点赞就添加
            articleLike.setArticleId(articleId);
            articleLike.setUserId(currentId);
            save(articleLike);

            //点赞数+1
            article.setLikeCount(article.getLikeCount()+1);
            articleService.updateById(article);
        }
        //更新ES中点赞数
        //1.准备Request
        UpdateRequest esRequest = new UpdateRequest("article", articleId.toString());
        //准备参数
        esRequest.doc(
                "likeCount",article.getLikeCount()
        );
        //2.发送请求
        try {
            client.update(esRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return R.success("点赞成功");
    }

    @Override
    public R<List<Article>> getAllLike(HttpServletRequest request) {
        LambdaQueryWrapper<ArticleLike> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(ArticleLike::getUserId,UserHolder.getCurrentId(request,stringRedisTemplate));
        List<ArticleLike> articleLikes = list(queryWrapper);

        // 提取点赞记录中的 articleId
        List<Long> articleIds = articleLikes.stream().map(ArticleLike::getArticleId).collect(Collectors.toList());

        // 查询 article 表，获取点赞的文章详细信息
        LambdaQueryWrapper<Article> articleQueryWrapper = new LambdaQueryWrapper<>();
        articleQueryWrapper.in(Article::getArticleId, articleIds);
        List<Article> likedArticleDetails = articleService.list(articleQueryWrapper);
        return R.success(likedArticleDetails);
    }
}
