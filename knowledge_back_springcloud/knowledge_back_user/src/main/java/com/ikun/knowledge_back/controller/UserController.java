package com.ikun.knowledge_back.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ikun.knowledge_back.common.CustomException;
import com.ikun.knowledge_back.common.R;
import com.ikun.knowledge_back.common.UserHolder;
import com.ikun.knowledge_back.dto.LoginFormDTO;
import com.ikun.knowledge_back.dto.RegisterFormDTO;
import com.ikun.knowledge_back.dto.UserDTO;
import com.ikun.knowledge_back.entity.User;
import com.ikun.knowledge_back.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/user0/user")
public class UserController {
    @Autowired
    private UserService userService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 发送手机验证码
     */
    @PostMapping("/code")
    public R<String> sendCode(@RequestParam("phoneOrEmail") String phoneOrEmail, HttpServletRequest request) {
        return userService.sendCode(phoneOrEmail,request);
    }

    //注册功能
    @PostMapping("/register")
    public R<String> register(@RequestBody RegisterFormDTO registerForm,HttpServletRequest request){
        return userService.register(registerForm,request);
    }

    /**
     * 验证码登录
     * @param loginForm 登录参数，包含手机号、验证码；或者手机号、密码
     */
    @PostMapping("/loginByCode")
    public R<String> loginByCode(@RequestBody LoginFormDTO loginForm,HttpServletRequest request){
        return userService.loginByCode(loginForm,request);
    }

    /**
     * 密码登录
     * @param loginForm 登录参数，包含手机号、验证码；或者手机号、密码
     */
    @PostMapping("/loginByPwd")
    public R<String> loginByPwd(@RequestBody LoginFormDTO loginForm,HttpServletRequest request){
        return userService.loginByPwd(loginForm,request);
    }

    //找回密码功能
    @PutMapping("/foundPwd")
    public R<String> foundPwd(@RequestBody RegisterFormDTO registerForm,HttpServletRequest request){
        return userService.foundPwd(registerForm,request);
    }

    //编辑资料
    @PutMapping("/updateInformation")
    public R<String> updateInformation(@RequestBody User user,HttpServletRequest request){
        return userService.updateInformation(user,request);
    }

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
