package com.ikun.knowledge_back.service.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.ikun.knowledge_back.common.JacksonObjectMapper;
import com.ikun.knowledge_back.common.R;
import com.ikun.knowledge_back.doc.ArticleDoc;
import com.ikun.knowledge_back.dto.*;
import com.ikun.knowledge_back.entity.*;
import com.ikun.knowledge_back.mapper.ArticleMapper;
import com.ikun.knowledge_back.service.*;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
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
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ArticleServiceImpl extends ServiceImpl<ArticleMapper, Article> implements ArticleService {

    @Autowired
    private RestHighLevelClient client;  //操纵es的

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

    @Override
    public Boolean insertESById(String articleDocJson) {
        if(StrUtil.isEmpty(articleDocJson)){
            return false;
        }
        JacksonObjectMapper jacksonObjectMapper=new JacksonObjectMapper();
        ArticleDoc articleDoc=null;
        try {
            articleDoc = jacksonObjectMapper.readValue(articleDocJson, ArticleDoc.class);
        } catch (JsonProcessingException e) {
            log.error(e.toString());
            return false;
        }
        //1.准备request对象    而不是发送单个请求到Elasticsearch，使用批量请求。这样，要么所有文档都成功索引，要么一个都不成功
        BulkRequest bulkRequest = new BulkRequest();
        bulkRequest.add(new IndexRequest("article").id(articleDoc.getArticleId().toString())
                .source(JSON.toJSONString(articleDoc), XContentType.JSON));
        //3.发送请求
        try {
            client.bulk(bulkRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            log.error(e.toString());
            return false;
        }
        return true;
    }

    @Override
    public Boolean deleteESById(Long articleId) {
        //1.准备Request
        DeleteRequest request = new DeleteRequest("article", articleId.toString());
        //2.发送请求
        try {
            client.delete(request,RequestOptions.DEFAULT);
        } catch (IOException e) {
            log.error(e.toString());
            return false;
        }
        return true;
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
