package com.ikun.knowledge_back.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ikun.knowledge_back.common.R;
import com.ikun.knowledge_back.dto.LoginFormDTO;
import com.ikun.knowledge_back.dto.RegisterFormDTO;
import com.ikun.knowledge_back.entity.User;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;

@Service
public interface UserService extends IService<User> {
    R<String> sendCode(String phoneOrEmail, HttpServletRequest request);

    R<String> register(RegisterFormDTO registerForm, HttpServletRequest request);

    R<String> loginByCode(LoginFormDTO loginForm, HttpServletRequest request);

    R<String> loginByPwd(LoginFormDTO loginForm, HttpServletRequest request);

    R<String> foundPwd(RegisterFormDTO registerForm, HttpServletRequest request);

    R<String> updateInformation(User user, HttpServletRequest request);
}
