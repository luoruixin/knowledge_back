package com.ikun.knowledge_back.interceptor;

import cn.hutool.core.util.StrUtil;
import com.ikun.knowledge_back.common.UserHolder;
import com.ikun.knowledge_back.common.UserHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class LoginInterceptor implements HandlerInterceptor {
    private StringRedisTemplate stringRedisTemplate;

    public LoginInterceptor(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate=stringRedisTemplate;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //判断是否需要拦截(ThreadLocal中是否有用户)
        if(UserHolder.getCurrentId(request,stringRedisTemplate)==null){
            //没有，需要拦截
            response.setStatus(401);
            //拦截
            return false;
        }
        //有用户，则放行
        return true;
    }

}
