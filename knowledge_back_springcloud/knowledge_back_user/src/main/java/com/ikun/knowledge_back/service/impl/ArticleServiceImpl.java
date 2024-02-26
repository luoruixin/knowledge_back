package com.ikun.knowledge_back.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ikun.knowledge_back.common.CustomException;
import com.ikun.knowledge_back.common.R;
import com.ikun.knowledge_back.common.UserHolder;
import com.ikun.knowledge_back.doc.ArticleDoc;
import com.ikun.knowledge_back.dto.*;
import com.ikun.knowledge_back.entity.*;
import com.ikun.knowledge_back.mapper.ArticleCommentMapper;
import com.ikun.knowledge_back.mapper.ArticleMapper;
import com.ikun.knowledge_back.service.*;
import com.ikun.knowledge_back.utils.HtmlToText;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.elasticsearch.search.suggest.completion.CompletionSuggestion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ArticleServiceImpl extends ServiceImpl<ArticleMapper, Article> implements ArticleService {
    @Autowired
    private ArticleMapper articleMapper;
    @Autowired
    private RelationService relationService;
    @Autowired
    private ArticleTagService articleTagService;
    @Autowired
    private UserService userService;
    @Autowired
    private ArticleCommentService articleCommentService;
    @Autowired
    private ArticleCommentMapper articleCommentMapper;
    @Resource
    private StringRedisTemplate stringRedisTemplate; //操纵redis的
    @Autowired
    private ArticleLikeService articleLikeService;
    @Autowired
    private ArticleCollectService articleCollectService;
    @Value("${knowledge_back.articleFolderPath}")
    private String articleFolderPath;


    //发布文章(同时存入ES和mysql，要保证操作的原子性)，这里要同时操纵3张表
    @Override
    public ArticleDoc publishArticle(PublishArticleDTO publishArticleDTO, HttpServletRequest request) {
        String articleClass = publishArticleDTO.getArticleClass();
        String title = publishArticleDTO.getTitle();
        List<Long> relationArticleIds = publishArticleDTO.getRelationArticleIds();
        String articleContentHtml = publishArticleDTO.getArticleContent();
        List<String> tags = publishArticleDTO.getArticleTag();

        if(StrUtil.isEmpty(articleClass)
                ||StrUtil.isEmpty(title)
                ||StrUtil.isEmpty(articleContentHtml)) {
            return null;
        }
        String articleContentTxt= HtmlToText.toText(articleContentHtml);  //先将html文档转为txt

        //将article保存到数据库
        Article article=new Article();
        article.setUserId(UserHolder.getCurrentId(request,stringRedisTemplate));
        boolean isSaved = save(article);
        if(!isSaved){
            return null;
        }
        article.setArticleClass(articleClass);
        //TODO:这里记得改为待审核
        article.setArticleState("已发布");
        article.setArticleContent(article.getArticleId()+".html");
        article.setArticleTime(LocalDateTime.now());
        article.setTitle(title);
        article.setLikeCount(0);
        article.setCommentCount(0);
        article.setCollectCount(0);
        article.setScanCount(0);
        updateById(article);  //这里不能使用save

        //将Content保存为html
        String articlePath = articleFolderPath+article.getArticleId()+".html";
        FileUtil.writeUtf8String(articleContentHtml,articlePath);

        //将relation保存到数据库
        List<Relation> relations=new ArrayList<>();
        for (Long relationArticleId : relationArticleIds) {
            Relation relation=new Relation();
            relation.setRelatedArticleId(article.getArticleId());
            relation.setArticleId(relationArticleId);
//            relationService.save(relation);
            relations.add(relation);
        }
        relationService.saveBatch(relations);  //这里建议使用saveBatch，可以减少连接数据库的次数

        //将tag保存到数据库
        List<ArticleTag> articleTags=new ArrayList<>();
        for (String tag : tags) {
            ArticleTag articleTag=new ArticleTag();
            articleTag.setArticleId(article.getArticleId());
            articleTag.setTag(tag);
            articleTags.add(articleTag);
        }
        articleTagService.saveBatch(articleTags);

        //将article保存到ES
        ArticleDoc articleDoc=new ArticleDoc(article);
        articleDoc.setArticleContent(articleContentTxt);//这里要手动设置articleContentTxt
        articleDoc.setArticleTag(tags);
        articleDoc.setSuggestion();
//        //1.准备request对象    而不是发送单个请求到Elasticsearch，使用批量请求。这样，要么所有文档都成功索引，要么一个都不成功
//        BulkRequest bulkRequest = new BulkRequest();
//        bulkRequest.add(new IndexRequest("article").id(articleDoc.getArticleId().toString())
//                .source(JSON.toJSONString(articleDoc), XContentType.JSON));
//        //3.发送请求
//        try {
//            client.bulk(bulkRequest, RequestOptions.DEFAULT);
//        } catch (IOException e) {
//            log.error(e.toString());
//            return R.error("插入ES错误");
//        }
        return articleDoc;
    }


    @Override
    @Transactional    //这里要操纵多张表和多个数据库(逻辑删除)
    public Boolean deleteArticleById(Long id, HttpServletRequest request) {
        if(id==null){
            throw new RuntimeException("传入id为空");
        }
        //删除mysql
        Article article = getById(id);
        if("删除".equals(article.getArticleState())){
            throw new RuntimeException("该文章已被删除");
        }
        article.setArticleState("删除");
        updateById(article);

        return true;
    }

    /**
     * 查看自己的所有文章的功能，包括(已发布、草稿、删除...)
     *
     * @param page
     * @param pageSize
     * @param condition
     * @param request
     * @return
     */
    @Override
    public R<Page> getMyArticle(int page, int pageSize, String condition, HttpServletRequest request) {
        Long userId = UserHolder.getCurrentId(request,stringRedisTemplate);
        Page<Article> pageInfo=new Page(page,pageSize);
        //构造条件构造器
        LambdaQueryWrapper<Article> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(userId!=null,Article::getUserId, userId)
                .eq(Article::getArticleState,condition)   //这里根据condition查询文章
                .isNotNull(Article::getUserId).orderByDesc(Article::getArticleId);

        page(pageInfo,queryWrapper);
        List<Article> records = pageInfo.getRecords();
        for (Article article : records) {
            String articlePath = articleFolderPath+article.getArticleContent();
            String content = FileUtil.readString(articlePath, Charset.defaultCharset());
            article.setArticleContent(content);
        }
        pageInfo.setRecords(records);
        return R.success(pageInfo);
    }


    //这里记得将点赞数、评论数、浏览量等加入到redis中
    //TODO：这里的浏览量有缺陷
    @Cacheable(value = "articleDetailCache",key = "#articleId")
    @Override
    public R<ArticleDetailDTO> getDetails(Long articleId, HttpServletRequest request) {

        Long currentId = UserHolder.getCurrentId(request,stringRedisTemplate);
        if(articleId==null){
            return R.error("id为空");
        }
        ArticleDetailDTO articleDetailDTO=new ArticleDetailDTO();
        //获取文章
        Article article = getById(articleId);
        String articlePath = articleFolderPath+article.getArticleContent();//替换articleContent
        String content=null;
        try {
            content = FileUtil.readString(articlePath, Charset.defaultCharset());
        }catch (Exception e){
            System.out.println(e);
            throw new CustomException("文章未找到");
        }
        article.setArticleContent(content);
        articleDetailDTO.setArticle(article);

        //获取作者
        User author = userService.getById(article.getUserId());
        author.setPassword(null);  //设置密码不可见
        articleDetailDTO.setAuthor(author);

        //获取评论
        List<CommentDTO> commentDTOList = articleCommentMapper.getCommentDTO(article.getArticleId());
        articleDetailDTO.setArticleComments(commentDTOList);

        //查询是否点赞
        LambdaQueryWrapper<ArticleLike> queryWrapper=new LambdaQueryWrapper<>();
        if(currentId==null){
            //如果当前没登陆，直接置为false
            articleDetailDTO.setLike(false);
            articleDetailDTO.setCollected(false);
        }else {
            queryWrapper.eq(ArticleLike::getArticleId,articleId).eq(ArticleLike::getUserId,currentId);
            if(articleLikeService.getOne(queryWrapper)==null){
                //未点赞
                articleDetailDTO.setLike(false);
            }else {
                articleDetailDTO.setLike(true);
            }

            //查询是否收藏
            LambdaQueryWrapper<ArticleCollect> queryWrapper1=new LambdaQueryWrapper<>();
            queryWrapper1.eq(ArticleCollect::getArticleId,articleId).eq(ArticleCollect::getUserId,currentId);
            if(articleCollectService.getOne(queryWrapper1)==null){
                //未收藏
                articleDetailDTO.setCollected(false);
            }else {
                articleDetailDTO.setCollected(true);
            }
        }

        //浏览量加一
//        stringRedisTemplate.opsForValue().

        return R.success(articleDetailDTO);
    }
}
