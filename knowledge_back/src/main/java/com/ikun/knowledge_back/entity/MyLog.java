package com.ikun.knowledge_back.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("log")
public class MyLog implements Serializable {
    @TableId(value = "log_id",type = IdType.AUTO)
    private Long logId;
    @TableField(value = "user_id")
    private Long userId;
    @TableField(value = "log_time")
    private LocalDateTime logTime;

    @TableField(value = "action")
    private String action;
}
