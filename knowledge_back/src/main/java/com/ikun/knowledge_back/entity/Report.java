package com.ikun.knowledge_back.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

@Data
@TableName("report")
public class Report implements Serializable {
    @TableId(value = "report_id",type = IdType.AUTO)
    private Long reportId;
    @TableField(value = "article_id")
    private Long articleId;
    @TableField(value = "user_id")
    private Long userId;
    @TableField(value = "report_class")
    private String reportClass;
    @TableField(value = "report_content")
    private String reportContent;
    @TableField(value = "report_state")
    private String reportState;
}
