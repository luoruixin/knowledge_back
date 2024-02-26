package com.ikun.knowledge_back.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.baidu.aip.nlp.AipNlp;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
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
import com.ikun.knowledge_back.mapper.RelationMapper;
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
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ArticleServiceImpl extends ServiceImpl<ArticleMapper, Article>  implements ArticleService {
    @Autowired
    private ArticleMapper articleMapper;
    @Autowired
    private RelationService relationService;
    @Autowired
    private RelationMapper relationMapper;
    @Autowired
    private ArticleTagService articleTagService;
    @Autowired
    private UserService userService;
    @Autowired
    private ArticleCommentService articleCommentService;
    @Autowired
    private ArticleCommentMapper articleCommentMapper;
    @Autowired
    private RestHighLevelClient client;  //操纵es的
    @Resource
    private StringRedisTemplate stringRedisTemplate; //操纵redis的
    @Autowired
    private ArticleLikeService articleLikeService;
    @Autowired
    private ArticleCollectService articleCollectService;
    @Value("${knowledge_back.articleFolderPath}")
    private String articleFolderPath;




    @Override
    public R<SearchResult> searchByKeyword(SearchFromDTO searchFromDTO, HttpServletRequest request) {
        SearchRequest esRequest=new SearchRequest("article");
        //关键字搜索
        String key=searchFromDTO.getKey();
        String condition="已发布";
        buildBasicQuery(searchFromDTO, esRequest, key, condition);

        //排序
        if(StrUtil.isEmpty(searchFromDTO.getSortBy())){
            //默认排序，这里不用写
        }else if(searchFromDTO.getSortBy().equals("time")){
            esRequest.source().sort("articleTime",SortOrder.DESC);
        }else if(searchFromDTO.getSortBy().equals("heat")){
            esRequest.source().sort("likeCount",SortOrder.DESC);
        }

        //分页
        int page = searchFromDTO.getPage();
        int size = searchFromDTO.getSize();
        esRequest.source().from((page-1)*size).size(size);

        //3.发送请求
        SearchResponse response = null;
        try {
            response = client.search(esRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        //将传递过来的参数进行高亮显示
        if(key==null||"".equals(key)){
            return parseResp(response,null);
        }else {
            return parseResp(response,"title","articleContent");  //对这两个字段进行高亮
        }
    }

    @Override
    public R<List<String>> getSuggestions(String prefix, HttpServletRequest request) throws IOException {
        //1.准备request
        SearchRequest esRequest = new SearchRequest("article");
        //2.准备DSL
        esRequest.source().suggest(new SuggestBuilder().addSuggestion(
                "suggestions",
                SuggestBuilders.completionSuggestion("suggestion")
                        .prefix(prefix)
                        .skipDuplicates(true)
                        .size(40)
        ));
        //3.发起请求
        SearchResponse response = client.search(esRequest, RequestOptions.DEFAULT);
        //4.解析结果
        Suggest suggest = response.getSuggest();
        //4.1根据补全查询名称suggestions获取补全结果
        CompletionSuggestion suggestions = suggest.getSuggestion("suggestions");
        //4.2获取options
        List<CompletionSuggestion.Entry.Option> options = suggestions.getOptions();
        List<String> list=new ArrayList<>(options.size());
        for (CompletionSuggestion.Entry.Option option : options) {
            String text = option.getText().toString();
            list.add(text);
        }
        return R.success(list);
    }

    //发布文章(同时存入ES和mysql，要保证操作的原子性)，这里要同时操纵3张表
    @Transactional      //保证操作的原子性
    @Override
    public R<String> publishArticle(PublishArticleDTO publishArticleDTO, HttpServletRequest request) {
        Long articleId = publishArticleDTO.getArticleId();
        String articleClass = publishArticleDTO.getArticleClass();
        String title = publishArticleDTO.getTitle();
        List<Long> relationArticleIds = publishArticleDTO.getRelationArticleIds();
        String articleContentHtml = publishArticleDTO.getArticleContent();
        List<String> tags = publishArticleDTO.getArticleTag();
        String articleState = publishArticleDTO.getArticleState();  //未来的状态

        if(articleId!=null){
            return R.error("不能传入id");
        }
        if(StrUtil.isEmpty(articleClass)
                ||StrUtil.isEmpty(title)
                ||StrUtil.isEmpty(articleContentHtml)) {
            return R.error("请将信息填充完整");
        }
        String articleContentTxt= HtmlToText.toText(articleContentHtml);  //先将html文档转为txt

        //将article保存到数据库
        Article article=new Article();
        article.setUserId(UserHolder.getCurrentId(request,stringRedisTemplate));
        boolean isSaved = save(article);
        if(!isSaved){
            return R.error("保存失败");
        }
        article.setArticleClass(articleClass);
        //TODO:这里记得改为待审核
        article.setArticleState(articleState);
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
        //1.准备request对象    而不是发送单个请求到Elasticsearch，使用批量请求。这样，要么所有文档都成功索引，要么一个都不成功
        BulkRequest bulkRequest = new BulkRequest();
        bulkRequest.add(new IndexRequest("article").id(articleDoc.getArticleId().toString())
                .source(JSON.toJSONString(articleDoc), XContentType.JSON));
        //3.发送请求
        try {
            client.bulk(bulkRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            log.error(e.toString());
            throw new CustomException("插入ES错误");
        }
        return R.success("文档发布成功");
    }

    @Override
    @Transactional
    public R<String> editArticle(PublishArticleDTO publishArticleDTO, HttpServletRequest request) {
        Long articleId = publishArticleDTO.getArticleId();
        String articleClass = publishArticleDTO.getArticleClass();
        String title = publishArticleDTO.getTitle();
        List<Long> relationArticleIds = publishArticleDTO.getRelationArticleIds();
        String articleContentHtml = publishArticleDTO.getArticleContent();
        List<String> tags = publishArticleDTO.getArticleTag();
        String articleState = publishArticleDTO.getArticleState();  //未来的状态

        if(StrUtil.isEmpty(articleClass)
                ||StrUtil.isEmpty(title)
                ||StrUtil.isEmpty(articleContentHtml)||articleId==null) {
            return R.error("请将信息填充完整");
        }
        String articleContentTxt= HtmlToText.toText(articleContentHtml);  //先将html文档转为txt

        //将article保存到数据库
        Article article=getById(articleId);
        if(article==null){
            return R.success("文章不存在");
        }
        article.setUserId(UserHolder.getCurrentId(request,stringRedisTemplate));
        article.setArticleClass(articleClass);
        //TODO:这里记得改为待审核
        article.setArticleState(articleState);
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

        //将relation保存到数据库(先删除，再新增)
        //先删除
        LambdaQueryWrapper<Relation> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(Relation::getRelatedArticleId,articleId);
        relationService.remove(queryWrapper);
        //再新增
        List<Relation> relations=new ArrayList<>();
        for (Long relationArticleId : relationArticleIds) {
            Relation relation=new Relation();
            relation.setRelatedArticleId(article.getArticleId());
            relation.setArticleId(relationArticleId);
//            relationService.save(relation);
            relations.add(relation);
        }
        relationService.saveBatch(relations);  //这里建议使用saveBatch，可以减少连接数据库的次数

        //将tag保存到数据库(先删除，再新增)
        //先删除
        LambdaQueryWrapper<ArticleTag> queryWrapper1=new LambdaQueryWrapper<>();
        queryWrapper1.eq(ArticleTag::getArticleId,articleId);
        articleTagService.remove(queryWrapper1);
        //再新增
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
        //1.准备request对象    而不是发送单个请求到Elasticsearch，使用批量请求。这样，要么所有文档都成功索引，要么一个都不成功
        BulkRequest bulkRequest = new BulkRequest();
        bulkRequest.add(new IndexRequest("article").id(articleDoc.getArticleId().toString())
                .source(JSON.toJSONString(articleDoc), XContentType.JSON));
        //3.发送请求
        try {
            client.bulk(bulkRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            log.error(e.toString());
            throw new CustomException("插入ES错误");
        }
        return R.success("文档编辑成功");
    }

    @Override
    public R<Page> getCheckArticle(int page, int pageSize, HttpServletRequest request) {
        Page<ArticleCheckDTO> pageInfo=new Page<>(page,pageSize);
        List<ArticleCheckDTO> articleCheckDTOS = articleMapper.pageCheckArticle(page - 1, pageSize);
        pageInfo.setRecords(articleCheckDTOS);
        pageInfo.setTotal(articleCheckDTOS.size());
        return R.success(pageInfo);
    }

    @Override
    public R<String> checkArticle(Long articleId, String articleState, HttpServletRequest request) {

        Article article = getById(articleId);
        article.setArticleState(articleState);
        updateById(article);

        //修改ES中文章的状态
        //1.准备Request
        UpdateRequest esRequest = new UpdateRequest("article", String.valueOf(articleId));
        //2.准备请求参数
        esRequest.doc("articleState",articleState);
        //3.发送请求
        try {
            client.update(esRequest,RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return R.success("文章已发布");
    }
    //@Override
    @Transactional
    public R<String> changeArticleState(Long articleId, String articleState, HttpServletRequest request) {
        if(articleId==null||StrUtil.isEmpty(articleState)){
            return R.error("信息填充完整");
        }
        Article article = getById(articleId);
        article.setArticleState(articleState);
        updateById(article);

        //修改ES中文章的状态
        //1.准备Request
        UpdateRequest esRequest = new UpdateRequest("article", String.valueOf(articleId));
        //2.准备请求参数
        esRequest.doc("articleState",articleState);
        //3.发送请求
        try {
            client.update(esRequest,RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return R.success("状态已改变");
    }
    @Override
    public R<List<String>> getTags(String title, String content) {
        String apiID="47748956";
        String apiKey = "XGXSaol3nFp56wHNDKIiW4NL";
        String secretKey = "vD27H6LNs5Kwh26ag92B3ZDC0Vqkhjrp";
        // 创建SDK客户端

        AipNlp client = new AipNlp(apiID,apiKey, secretKey);

        // 设置可选参数
        HashMap<String, Object> options = new HashMap<String,Object>();
        JSONObject res = client.keyword(title, content, options);
        JSONArray items = res.getJSONArray("items");
        List<String> tags= new ArrayList<>();
        for (int i=0;i<items.length();i++){
            JSONObject item = items.getJSONObject(i);
            tags.add(item.getString("tag"));
        }
        return R.success(tags);
    }


    @Override
    @Transactional    //这里要操纵多张表和多个数据库(逻辑删除)
    public R<String> deleteArticleById(Long id, HttpServletRequest request) {
        if(id==null){
            return R.error("传入id为空");
        }
        //删除mysql
        Article article = getById(id);
        if("删除".equals(article.getArticleState())){
            return R.error("该文章已被删除");
        }
        article.setArticleState("删除");
        updateById(article);

        //更新索引库为删除
        //1.准备Request
        UpdateRequest esRequest = new UpdateRequest("article", id.toString());
        //准备参数
        esRequest.doc(
                "articleState","删除"
        );
        //2.发送请求
        try {
            client.update(esRequest,RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return null;
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
        if(StrUtil.isEmpty(condition)){
            //condition=null返回全部，除了被删除和被举报
            queryWrapper.eq(userId!=null,Article::getUserId, userId)
                    .ne(Article::getArticleState,"被举报")
                    .ne(Article::getArticleState,"删除")
                    .isNotNull(Article::getUserId).orderByDesc(Article::getArticleId);
        }else {
            queryWrapper.eq(userId!=null,Article::getUserId, userId)
                    .eq(Article::getArticleState,condition)   //这里根据condition查询文章
                    .isNotNull(Article::getUserId).orderByDesc(Article::getArticleId);
        }


        page(pageInfo,queryWrapper);
        List<Article> records = pageInfo.getRecords();
        for (Article article : records) {
            String articlePath = articleFolderPath+article.getArticleContent();
            try {
                String content = FileUtil.readString(articlePath, Charset.defaultCharset());
                article.setArticleContent(content);
            }catch (Exception e){
                article.setArticleContent("");
            }

        }
        pageInfo.setRecords(records);
        return R.success(pageInfo);
    }


    //这里记得将点赞数、评论数、浏览量等加入到redis中
    //TODO：这里的浏览量有缺陷
    //TODO: 添加
//    @Cacheable(value = "articleDetailCache",key = "#articleId")
    @Override
    public R<ArticleDetailDTO> getDetails(Long articleId, String condition, HttpServletRequest request) {

        Long currentId = UserHolder.getCurrentId(request,stringRedisTemplate);
        if(articleId==null){
            return R.error("id为空");
        }
        ArticleDetailDTO articleDetailDTO=new ArticleDetailDTO();
        //获取文章
        Article article = getById(articleId);
        if(article==null){
            return R.error("文章不存在");
        }

        //如果该文章不是该状态
//        if(!article.getArticleState().equals(condition)){
//            return R.error("该文章存在问题");
//        }
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
        List<CommentDTO> commentDTOList = articleCommentMapper.getCommentDTO(articleId);
        articleDetailDTO.setArticleComments(commentDTOList);

        //获取关联文章
        List<RelationArticleDTO> relationArticles = new ArrayList<>();
        List<Relation> relationList = relationService.getRelationsByArticleId(articleId);
        for(Relation relation: relationList){
            Long relationId = relation.getRelationId();
            Long relatedArticleId = relation.getRelatedArticleId();
            Article relatedArticle = getById(relatedArticleId);
            String relatedArticleTitle = relatedArticle.getTitle();

            RelationArticleDTO relationArticleDTO = new RelationArticleDTO();
            relationArticleDTO.setRelationArticleId(relationId);
            relationArticleDTO.setRelatedArticleId(relatedArticleId);
            relationArticleDTO.setRelatedArticleTitle(relatedArticleTitle);

            relationArticles.add(relationArticleDTO);
        }
        articleDetailDTO.setRelationArticles(relationArticles);

//        for ()

        //获取文章标签
        QueryWrapper<ArticleTag> articleTagQueryWrapper = new QueryWrapper<>();
        articleTagQueryWrapper.eq("article_id",articleId);
        List<ArticleTag> articleTagList = articleTagService.list(articleTagQueryWrapper);
        articleDetailDTO.setArticleTagList(articleTagList);

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
            List<ArticleCollect> articleCollectList = articleCollectService.list(queryWrapper1);
            if(articleCollectList==null||articleCollectList.size()==0){
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

    @Override
    public R<List<ArticleWithCommentDTO>> getArticleWithComment(HttpServletRequest request) {
        Long userId = UserHolder.getCurrentId(request,stringRedisTemplate);
        LambdaQueryWrapper<Article> queryWrapper =new LambdaQueryWrapper<>();
        queryWrapper.eq(Article::getUserId, userId).and(wrapper -> wrapper.gt(Article::getCommentCount, 0));
        List<Article> articles = articleMapper.selectList(queryWrapper);
        List<ArticleWithCommentDTO> articleWithCommentDTOS = new ArrayList<>();
        for (Article article : articles) {
            ArticleWithCommentDTO articleWithCommentDTO=new ArticleWithCommentDTO();
            long articleId =article.getArticleId();
            List<CommentDTO> commentDTO = articleCommentMapper.getCommentDTO(articleId);
            BeanUtils.copyProperties(article,articleWithCommentDTO);
            articleWithCommentDTO.setArticleCommentList(commentDTO);
            articleWithCommentDTOS.add(articleWithCommentDTO);
        }
        return R.success(articleWithCommentDTOS);
    }

    @Override
    public R<String> changeReportArticleState(Long articleId, String targetState){
        UpdateWrapper<Article> articleUpdateWrapper = new UpdateWrapper<>();
        articleUpdateWrapper.eq("article_id", articleId);
        articleUpdateWrapper.set("article_state", "已退回");
        Article article = new Article();
        baseMapper.update(article, articleUpdateWrapper);

        return R.success("退回成功");
    }




    //===========================工具类=================================
    //封装request工具类（使用bool查询封装多个子查询）
    /**
     *
     * @param searchFromDTO
     * @param request
     * @param key
     * @param condition   条件：已发布，草稿.........
     */
    private static void buildBasicQuery(SearchFromDTO searchFromDTO, SearchRequest request, String key, String condition) {
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        if(key ==null||"".equals(key)){
            //前端没有传过来参数，使用全文检索查询
            boolQuery.must(QueryBuilders.matchAllQuery());
        }else {
            // 进行算分 //TODO:重点测试算分
            FunctionScoreQueryBuilder functionScoreQuery = QueryBuilders.functionScoreQuery(
                    QueryBuilders.matchQuery("all", key),
                    new FunctionScoreQueryBuilder.FilterFunctionBuilder[]{
                            new FunctionScoreQueryBuilder.FilterFunctionBuilder(
                                    QueryBuilders.existsQuery("title"),
                                    ScoreFunctionBuilders.weightFactorFunction(3)
                            ),
                            //articleClass这里应该可以不要
                            new FunctionScoreQueryBuilder.FilterFunctionBuilder(
                                    QueryBuilders.existsQuery("articleClass"),
                                    ScoreFunctionBuilders.weightFactorFunction(2)
                            ),
                            new FunctionScoreQueryBuilder.FilterFunctionBuilder(
                                    QueryBuilders.existsQuery("articleContent"),
                                    ScoreFunctionBuilders.weightFactorFunction(1)
                            ),
                            new FunctionScoreQueryBuilder.FilterFunctionBuilder(
                                    QueryBuilders.existsQuery("articleTag"),
                                    ScoreFunctionBuilders.weightFactorFunction(2)
                            )
                    }
            );
            boolQuery.must(functionScoreQuery);

//            boolQuery.must(QueryBuilders.matchQuery("all", key));

            //高亮
            request.source().highlighter(new HighlightBuilder().
                    field("title").field("articleContent").
                    requireFieldMatch(false)
            );
        }
        //类别条件
        if(searchFromDTO.getArticleClass()!=null&&!searchFromDTO.getArticleClass().equals("")){
            boolQuery.filter(QueryBuilders.termQuery("articleClass", searchFromDTO.getArticleClass()));
        }
        // articleState条件
        boolQuery.filter(QueryBuilders.termQuery("articleState", condition));

        request.source().query(boolQuery);

    }

    //解析返回json的工具类(并将高亮部分进行替换)
    public static R<SearchResult> parseResp(SearchResponse response, String... highlightKeys){
        //获取hits
        SearchHits searchHits = response.getHits();
        //获取总条数
        long total = searchHits.getTotalHits().value;
        System.out.println("共搜索到"+total+"条数据");
        //拿到文档数组
        SearchHit[] hits = searchHits.getHits();
        List<ArticleDoc> articleDocs=new ArrayList<>();
        for (SearchHit hit : hits) {
            //获取文档source
            String json = hit.getSourceAsString();
            //反序列化
            ArticleDoc articleDoc = JSON.parseObject(json, ArticleDoc.class);
            Object[] sortValues = hit.getSortValues();

            //获取高亮结果
            Map<String, HighlightField> highlightFields = hit.getHighlightFields();
            if(highlightFields==null||highlightFields.size()==0){ //判断是否为空
                articleDocs.add(articleDoc);
                continue;
            }

            for (String highlightKey : highlightKeys) {
                //根据字段名获取高亮结果
                HighlightField highlightField = highlightFields.get(highlightKey);
                if(highlightField!=null&&highlightKey.equals("title")){
                    Text fragment = highlightField.getFragments()[0];
                    //获取高亮值
                    String highlightValue = fragment.string();
                    articleDoc.setTitle(highlightValue);
                }

                if(highlightField!=null&&highlightKey.equals("articleContent")){
                    Text fragment = highlightField.getFragments()[0];
                    //获取高亮值
                    String highlightValue = fragment.string();
                    articleDoc.setArticleContent(highlightValue);
                }
            }
            articleDocs.add(articleDoc);
        }
        //封装返回
        return R.success(new SearchResult(total,articleDocs));
    }
}
