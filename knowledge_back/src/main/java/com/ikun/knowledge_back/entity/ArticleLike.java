package com.ikun.knowledge_back.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("article_like")
public class ArticleLike {
    @TableId(value = "article_like_id",type = IdType.AUTO)
    private Long articleLikeId;
    @TableField(value = "article_id")
    private Long articleId;
    @TableField(value = "user_id")
    private Long userId;

    @TableField(value = "like_state")
    private String likeState;
}
