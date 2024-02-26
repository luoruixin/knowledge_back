package com.ikun.knowledge_back.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.ikun.knowledge_back.common.R;
import com.ikun.knowledge_back.dto.UserDTO;
import com.ikun.knowledge_back.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/admin/user")
public class UserController {
    @Autowired
    private UserService userService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;



    //查看个人资料
    @GetMapping("/getInformation")
    public R<UserDTO> getInformation(HttpServletRequest request){
        String token = request.getHeader("authorization");

// 2. 基于token获取redis中的用户
        Map<Object, Object> userMap = stringRedisTemplate.opsForHash()
                .entries("login:token:" + token);
        UserDTO userDTO = BeanUtil.fillBeanWithMap(userMap, new UserDTO(), false);
        return R.success(userDTO);
    }

    /**
     * 登出功能
     */
    @PostMapping("/loginOut")
    public R<String> loginOut(HttpServletRequest request){
        String token = request.getHeader("authorization");
        if(!StrUtil.isEmpty(token)){
            stringRedisTemplate.delete(token);
        }
        return R.success("登出成功");
    }
}
