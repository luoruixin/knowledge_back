package com.ikun.knowledge_back.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ikun.knowledge_back.common.R;
import com.ikun.knowledge_back.common.UserHolder;
import com.ikun.knowledge_back.dto.ArticleCollectDTO;
import com.ikun.knowledge_back.entity.Article;
import com.ikun.knowledge_back.entity.ArticleCollect;
import com.ikun.knowledge_back.entity.Collection;
import com.ikun.knowledge_back.mapper.ArticleCollectMapper;
import com.ikun.knowledge_back.mapper.CollectionMapper;
import com.ikun.knowledge_back.service.ArticleCollectService;
import com.ikun.knowledge_back.service.ArticleService;
import com.ikun.knowledge_back.service.CollectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

@Service
public class CollectionServiceImpl extends ServiceImpl<CollectionMapper, Collection> implements CollectionService {
    @Autowired
    private ArticleCollectService articleCollectService;
    @Autowired
    private ArticleCollectMapper articleCollectMapper;
    @Autowired
    private ArticleService articleService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Override
    public R<String> create(String collectionName, HttpServletRequest request) {
        if(StrUtil.isEmpty(collectionName)){
            return R.error("请填入收藏夹名称");
        }
        Collection collection=new Collection();
        collection.setUserId(UserHolder.getCurrentId(request,stringRedisTemplate));
        collection.setCollectionName(collectionName);
        save(collection);
        return R.success("建立收藏夹成功");
    }

    @Override
    public R<List<Collection>> getAllCollection(HttpServletRequest request) {
        LambdaQueryWrapper<Collection> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper
                .eq(Collection::getUserId,UserHolder.getCurrentId(request,stringRedisTemplate));
        List<Collection> collectionList = list(queryWrapper);
        return R.success(collectionList);
    }

    //收藏文章
    @Transactional
    @Override
    public R<String> collectArticle(ArticleCollectDTO articleCollectDTO, HttpServletRequest request) {
        Long articleId = articleCollectDTO.getArticleId();
        List<Long> collectionIds = articleCollectDTO.getCollectionIds();
        if(articleId==null
                ||collectionIds==null){
            return R.error("收藏夹信息不完整");
        }
        Long userId = UserHolder.getCurrentId(request,stringRedisTemplate);


        List<ArticleCollect> articleCollectList=new ArrayList<>();
        for (Long collectionId : articleCollectDTO.getCollectionIds()) {
            //先查询是否有这条记录
            LambdaQueryWrapper<ArticleCollect> queryWrapper=new LambdaQueryWrapper<>();
            queryWrapper
                    .eq(ArticleCollect::getUserId,userId)
                    .eq(ArticleCollect::getArticleId,articleId)
                    .eq(ArticleCollect::getCollectionId,collectionId);
            ArticleCollect articleCollectOne = articleCollectService.getOne(queryWrapper);
            if(articleCollectOne!=null){
                //已经在该收藏夹中收藏了该文章
                continue;
            }

            ArticleCollect articleCollect=new ArticleCollect();
            articleCollect.setArticleId(articleCollectDTO.getArticleId());
            articleCollect.setUserId(userId);
            articleCollect.setCollectionId(collectionId);

            articleCollect.setCollectState("存在");
            articleCollectList.add(articleCollect);
        }
        //
        articleCollectService.saveBatch(articleCollectList);

        //收藏数+1
        Article article = articleService.getById(articleId);
        article.setCollectCount(article.getCollectCount()+1);
        articleService.updateById(article);
        return R.success("收藏成功");
    }

    @Override
    public R<List<Article>> getCollectArticle(Long collectionId, HttpServletRequest request) {
        if(collectionId==null){
            return R.error("收藏夹id为空");
        }
        List<Article> articleList = articleCollectMapper.getCollectArticle(collectionId);
        return R.success(articleList);
    }
}
