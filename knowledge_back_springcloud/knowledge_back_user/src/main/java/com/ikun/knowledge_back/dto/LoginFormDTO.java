package com.ikun.knowledge_back.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class LoginFormDTO implements Serializable {
    private String phoneOrEmail;
    private String code;
    private String password;
}
