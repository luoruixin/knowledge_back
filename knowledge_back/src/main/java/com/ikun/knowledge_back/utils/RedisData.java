package com.ikun.knowledge_back.utils;

import lombok.Data;

import java.time.LocalDateTime;

//p45逻辑过期
@Data
public class RedisData {
    private LocalDateTime expireTime;
    private Object data; //这个data可以是shop
}
