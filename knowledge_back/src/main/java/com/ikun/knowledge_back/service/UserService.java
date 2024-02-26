package com.ikun.knowledge_back.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ikun.knowledge_back.common.R;
import com.ikun.knowledge_back.dto.AdminData4DTO;
import com.ikun.knowledge_back.dto.LoginFormDTO;
import com.ikun.knowledge_back.dto.RegisterFormDTO;
import com.ikun.knowledge_back.entity.User;

import javax.servlet.http.HttpServletRequest;

public interface UserService extends IService<User> {
    R<String> sendCode(String phoneOrEmail, HttpServletRequest request);

    R<String> register(RegisterFormDTO registerForm, HttpServletRequest request);

    R<String> loginByCode(LoginFormDTO loginForm, HttpServletRequest request);

    R<String> loginByPwd(LoginFormDTO loginForm, HttpServletRequest request);

    R<String> foundPwd(RegisterFormDTO registerForm, HttpServletRequest request);

    R<String> updateInformation(User user, HttpServletRequest request);
    R<String> updateAvatar(User user, HttpServletRequest request);
    R<String> updateName(User user, HttpServletRequest request);
    R<String> updateAge(User user, HttpServletRequest request);
    R<String> updateSex(User user, HttpServletRequest request);

    R<AdminData4DTO> adminData4(HttpServletRequest request);
}
