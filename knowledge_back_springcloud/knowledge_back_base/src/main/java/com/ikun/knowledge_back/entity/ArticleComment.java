package com.ikun.knowledge_back.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("article_comment")
public class ArticleComment implements Serializable {
    @TableId(value = "article_comment_id",type = IdType.AUTO)
    private Long articleCommentId;
    @TableField(value = "article_id")
    private Long articleId;
    @TableField(value = "user_id")
    private Long userId;

    @TableField(value = "comment_time")
    private LocalDateTime commentTime;
    @TableField(value = "comment_content")
    private String commentContent;
    @TableField(value = "comment_state")
    private String commentState;
}
