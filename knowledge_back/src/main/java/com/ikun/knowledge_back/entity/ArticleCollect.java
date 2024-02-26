package com.ikun.knowledge_back.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

@Data
@TableName("article_collect")
public class ArticleCollect implements Serializable {
    @TableId(value = "article_collect_id",type = IdType.AUTO)
    private Long articleCollectId;
    @TableField(value = "article_id")
    private Long articleId;
    @TableField(value = "user_id")
    private Long userId;
    @TableField(value = "collection_id")
    private Long collectionId;

    //TODO:该字段是否需要
    @TableField(value = "collect_state")
    private String collectState;

}
