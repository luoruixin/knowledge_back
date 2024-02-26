package com.ikun.knowledge_back;

import cn.hutool.core.io.FileUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ikun.knowledge_back.doc.ArticleDoc;
import com.ikun.knowledge_back.entity.Article;
import com.ikun.knowledge_back.entity.ArticleTag;
import com.ikun.knowledge_back.service.ArticleService;
import com.ikun.knowledge_back.service.ArticleTagService;
import org.apache.http.HttpHost;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
class UploadFilesToES {
    @Autowired
    private ArticleService articleService;
    @Autowired
    private ArticleTagService articleTagService;

    @Value("${knowledge_back.articleFolderPath}")
    private String articleFolderPath;
    @Value("${es.host}")
    private String esHost;

    private RestHighLevelClient restHighLevelClient;
    @Test
    void contextLoads() throws IOException {
        // 查询所有的酒店数据
        List<Article> list = articleService.list();

        // 1.准备Request
        BulkRequest request = new BulkRequest();
        // 2.准备参数
        for (Article article : list) {
            String articlePath = articleFolderPath+article.getArticleContent();
            String content = FileUtil.readString(articlePath, Charset.defaultCharset());
            article.setArticleContent(content);

            // 2.1.转为ArticleDoc
            ArticleDoc articleDoc = new ArticleDoc(article);

            QueryWrapper<ArticleTag> queryWrapper=new QueryWrapper<>();
            queryWrapper.eq("article_id", article.getArticleId());
            List<ArticleTag> articleTags = articleTagService.list(queryWrapper);
            List<String> articleTagsString=new ArrayList<>();
            for (ArticleTag articleTag : articleTags) {
                articleTagsString.add(articleTag.getTag());
            }
            articleDoc.setArticleTag(articleTagsString);
            articleDoc.setSuggestion();

            // 2.2.转json
            String json = JSON.toJSONString(articleDoc);
            // 2.3.添加请求  注意将id为string类型
            request.add(new IndexRequest("article").id(articleDoc.getArticleId().toString()).source(json, XContentType.JSON));
        }
        // 3.发送请求
        restHighLevelClient.bulk(request, RequestOptions.DEFAULT);
    }

    /**
     * 查询文档
     * @throws IOException
     */
    @Test
    void testGetDocumentById() throws IOException {
        //1.准备request对象
        GetRequest request = new GetRequest("article","1");//这里的id必须转为字符串
        // 2. 发送请求，得到响应
        GetResponse response = restHighLevelClient.get(request, RequestOptions.DEFAULT);
        // 3.解析响应结果
        String json = response.getSourceAsString();//得到json中的source字段(也是json)
        ArticleDoc articleDoc = JSON.parseObject(json, ArticleDoc.class);//将json反序列化为HotelDoc对象
        System.out.println(articleDoc);
    }

    @BeforeEach
    //在每个测试方法之前执行。（初始化es的客户端）
    void setUp(){
        this.restHighLevelClient=new RestHighLevelClient(RestClient.builder(
                HttpHost.create("http://"+esHost+":9200")
        ));
    }

    @AfterEach
//在每个测试方法之后执行。
    void tearDown() throws IOException {
        this.restHighLevelClient.close();
    }

}
