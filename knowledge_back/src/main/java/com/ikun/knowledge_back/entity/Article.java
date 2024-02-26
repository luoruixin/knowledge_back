package com.ikun.knowledge_back.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

// TODO: article
@Data
@TableName("article")
public class Article implements Serializable {
    @TableId(value = "article_id",type = IdType.AUTO)
    private Long articleId;
    @TableField(value = "title")
    private String title;
    @TableField(value = "user_id")
    private Long userId;
    @TableField(value = "like_count")
    private int likeCount;
    @TableField(value = "comment_count")
    private int commentCount;
    @TableField(value = "collect_count")
    private int collectCount;
    @TableField(value = "scan_count")
    private int scanCount;
    @TableField(value = "article_class")
    private String articleClass;
    @TableField(value = "article_content")
    private String articleContent;
    @TableField(value = "article_time")
    private LocalDateTime articleTime;
    @TableField(value = "article_state")
    private String articleState;

}
