package com.ikun.knowledge_back.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;

//和user类相比没有password
@Data
public class UserDTO implements Serializable {
    private Long userId;
    private String name;
    private String email;
    private String phone;
    private String sex;
    private String age;
    private String recommendation;
    private String userState;
    private String avatar;
}
