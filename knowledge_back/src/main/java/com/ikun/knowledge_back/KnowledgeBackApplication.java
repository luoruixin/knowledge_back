package com.ikun.knowledge_back;

//import net.logstash.logback.encoder.LogstashEncoder;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.transaction.annotation.EnableTransactionManagement;


// Generated by https://start.springboot.io
// 优质的 spring/boot/data/security/cloud 框架中文文档尽在 => https://springdoc.cn
@SpringBootApplication
@EnableTransactionManagement//开启事务注解的支持
@EnableCaching   //开启cache
public class KnowledgeBackApplication {

    public static void main(String[] args) {
        SpringApplication.run(KnowledgeBackApplication.class, args);
    }

    @Value("${es.host}")
    private String esHost;
    //从spring中注入es的client
    @Bean
    public RestHighLevelClient client(){
        return new RestHighLevelClient(RestClient.builder(
                HttpHost.create("http://"+esHost+":9200")
        ));
    }

    //TODO:logstash记得这里打开
//    @Bean
//    public Logger getLogger(){
//        return LoggerFactory.getLogger(KnowledgeBackApplication.class);
//    }
}
