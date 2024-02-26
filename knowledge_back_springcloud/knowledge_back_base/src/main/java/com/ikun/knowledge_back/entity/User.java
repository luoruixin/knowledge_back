package com.ikun.knowledge_back.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("user")
public class User implements Serializable {
    @TableId(value = "user_id",type = IdType.AUTO)
    private Long userId;
    @TableField(value = "name")
    private String name;
    @TableField(value = "email")
    private String email;
    @TableField(value = "phone")
    private String phone;
    @TableField(value = "password")
    private String password;
    @TableField(value = "sex")
    private String sex;
    @TableField(value = "age")
    private String age;
    @TableField(value = "recommendation")
    @Size(max = 128,message = "自我介绍长度不能超过128")
    private String recommendation;
    @TableField(value = "user_state")
    private String userState;
    @TableField(value = "avatar")
    private String avatar;
}
