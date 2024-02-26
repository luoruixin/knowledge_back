package com.ikun.knowledge_back;

import com.ikun.knowledge_back.utils.HtmlToText;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

@SpringBootTest
public class test01 {
    @Autowired
    private RedisTemplate redisTemplate;
    @Test
    void test01(){
        redisTemplate.opsForValue().set("k1","v1");
        System.out.println(redisTemplate.opsForValue().get("k1"));
    }

    @Test
    void test02(){
        String s = HtmlToText.toText("<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <title>关于<head>标签的讲解,</title>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <p>Hello World!你好</p>\n" +
                "</body>\n" +
                "</html>");
        System.out.println(s);
    }
}
