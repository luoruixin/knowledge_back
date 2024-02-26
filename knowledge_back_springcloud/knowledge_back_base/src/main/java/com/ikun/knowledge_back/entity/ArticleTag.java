package com.ikun.knowledge_back.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

@Data
@TableName("article_tag")
public class ArticleTag implements Serializable {
    @TableId(value = "article_tag_id",type = IdType.AUTO)
    private Long articleTagId;
    @TableField(value = "article_id")
    private Long articleId;
    @TableField(value = "tag")
    private String tag;

}
