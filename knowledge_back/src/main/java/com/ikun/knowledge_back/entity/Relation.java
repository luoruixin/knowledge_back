package com.ikun.knowledge_back.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

// TODO: Relation
@Data
@TableName("relation")
public class Relation implements Serializable {
    @TableId(value = "relation_id",type = IdType.AUTO)
    private Long relationId;
    @TableField(value = "related_article_id")
    private Long relatedArticleId;
    @TableField(value = "article_id")
    private Long articleId;
}
