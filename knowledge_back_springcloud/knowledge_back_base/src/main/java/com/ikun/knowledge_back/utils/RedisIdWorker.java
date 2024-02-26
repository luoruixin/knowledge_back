package com.ikun.knowledge_back.utils;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

//全局id生成器
@Component
public class RedisIdWorker {

    /**
     * 开始时间戳：2023.1.1
     */
    private static final long BEGIN_TIMESTAMP=1672531200L;

    /**
     * 序列号的位数（32位）
     */
    private static final int COUNT_BITS=32;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    public long nextId(String keyPrefix){  //keyPrefix是业务的前缀
        //// 1.生成时间戳(32位)
        LocalDateTime now = LocalDateTime.now();
        long nowSecond = now.toEpochSecond(ZoneOffset.UTC);//当前的秒数
        long timeStamp = nowSecond - BEGIN_TIMESTAMP;

        // 2.生成序列号
        // 2.1. 获取当前日期，精确到天
        String date = now.format(DateTimeFormatter.ofPattern("yyyy:MM:dd"));
        // 2.2. 自增长increment
        Long count = stringRedisTemplate.opsForValue().increment("icr:" + keyPrefix + ":" + date);

        // 3.拼接并返回
        return timeStamp<<COUNT_BITS | count;//高位事时间戳，低位是序列号
    }

    public static void main(String[] args) {
        LocalDateTime time=LocalDateTime.of(2023,1,1,0,0,0);
        System.out.println(time.toEpochSecond(ZoneOffset.UTC));
    }
}
