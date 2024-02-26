package com.ikun.knowledge_back.common;

public class MqConstants {
    //交换机
    public final static String ARTICLE_EXCHANGE="article.topic";

    //监昕新增和修改的队列
    public final static String ARTICLE_INSERT_QUEUE="article.insert.queue";

    //监昕删除的队列
    public final static String ARTICLE_DELETE_QUEUE="article.delete.queue";

    //新增或修改的RoutingKey
    public final static String ARTICLE_INSERT_KEY="article.insert";

    //删除的RoutingKey
    public final static String ARTICLE_DELETE_KEY="article.delete";
}
