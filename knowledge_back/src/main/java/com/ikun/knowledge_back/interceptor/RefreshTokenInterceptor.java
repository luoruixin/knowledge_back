package com.ikun.knowledge_back.interceptor;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.ikun.knowledge_back.common.UserHolder;
import com.ikun.knowledge_back.dto.UserDTO;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.concurrent.TimeUnit;

//专门用于刷新token有效期的拦截器
public class RefreshTokenInterceptor implements HandlerInterceptor {
    //这里不能使用@Resource或者@Autowired来注入，因为LoginInterceptor类不受spring管理
    //必须使用构造函数注入
    private StringRedisTemplate stringRedisTemplate;


    public RefreshTokenInterceptor(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }
//    @Resource
//    private StringRedisTemplate stringRedisTemplate;

    public RefreshTokenInterceptor() {
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1.TODO 获取请求头中的token,header是authorization
        String token = request.getHeader("authorization");
        if(StrUtil.isBlank(token)){
            //此时放行ThreadLocal中没有用户，会被下一个拦截器拦截
            return true;//放行
        }
        // 2. 基于token获取redis中的用户
        Map<Object, Object> userMap = stringRedisTemplate.opsForHash()
                .entries("login:token:" + token);
        // 3. 判断用户是否存在
        if (userMap.isEmpty()) {
            //此时放行ThreadLocal中没有用户，会被下一个拦截器拦截
            return true;//放行
        }

        //刷新token有效期,否则有效期会过时
        stringRedisTemplate.expire("login:token:" + token, 30,TimeUnit.MINUTES);
        //6.放行
        return true;
    }
}
