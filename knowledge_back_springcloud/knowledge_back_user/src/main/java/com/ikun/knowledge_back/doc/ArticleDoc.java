package com.ikun.knowledge_back.doc;

import com.ikun.knowledge_back.entity.Article;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

//该类在article的基础上加上了suggestion字段和tag字段
@Data
@NoArgsConstructor
public class ArticleDoc {
    @Value("${knowledge_back.articleFolderPath}")
    private String articleFolderPath;
    private Long articleId;
    private String title;
    private Long userId;
    private int likeCount;
    private int commentCount;
    private int collectCount;
    private int scanCount;
    private String articleClass;
    private String articleContent;
    private LocalDateTime articleTime;
    private String articleState;
    private List<String> articleTag;
    private List<String> suggestion;

    public ArticleDoc(Article article){
        this.articleId=article.getArticleId();
        this.title=article.getTitle();
        this.userId=article.getUserId();
        this.likeCount=article.getLikeCount();
        this.commentCount=article.getCommentCount();
        this.collectCount=article.getCollectCount();
        this.scanCount=article.getScanCount();
        this.articleClass=article.getArticleClass();
        //这里要手动设置articleContent（与article不同）

        this.articleTime=article.getArticleTime();
        this.articleState=article.getArticleState();

    }

    //这里可以指定把什么字段加入到联想的范围内
    public void setSuggestion() {
        this.suggestion = new ArrayList<>();
        this.suggestion.add(this.title);
        this.suggestion.add(this.articleClass);
        this.suggestion.addAll(this.articleTag);
    }

    //设置articleContent(与article中的不同)!!!!!!!!!
}
