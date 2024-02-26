package com.ikun.knowledge_back.common;


import cn.hutool.core.util.StrUtil;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;
@Data
public class UserHolder {

    private StringRedisTemplate stringRedisTemplate;

    public static Long getCurrentId(HttpServletRequest request,StringRedisTemplate stringRedisTemplate){
        String token = request.getHeader("authorization");
        // 2. 基于token获取redis中的用户
        Map<Object, Object> userMap = stringRedisTemplate.opsForHash()
                .entries("login:token:" + token);
        String id = (String) userMap.get("userId");
        if(StrUtil.isEmpty(id)){
            return null;
        }
        Long userId = Long.parseLong(id);
        return userId;
    }
}
