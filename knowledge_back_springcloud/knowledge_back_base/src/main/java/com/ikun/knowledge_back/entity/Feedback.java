package com.ikun.knowledge_back.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

@Data
@TableName("feedback")
public class Feedback implements Serializable {
    @TableId(value = "feedback_id",type = IdType.AUTO)
    private Long feedbackId;
    @TableField(value = "user_id")
    private Long userId;
    @TableField(value = "feedback_object")
    private String feedbackObject;

    @TableField(value = "feedback_content")
    private String feedbackContent;
    @TableField(value = "feedback_state")
    private String feedbackState;

}
