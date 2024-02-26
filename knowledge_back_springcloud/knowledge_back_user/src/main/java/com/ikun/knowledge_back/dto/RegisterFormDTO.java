package com.ikun.knowledge_back.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class RegisterFormDTO implements Serializable {
    private String name;
    private String phoneOrEmail;
    private String code;
    private String password;
}
