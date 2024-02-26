package com.ikun.knowledge_back.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

@Data
@TableName("collection")
public class Collection implements Serializable {
    @TableId(value = "collection_id",type = IdType.AUTO)
    private Long collectionId;
    @TableField(value = "user_id")
    private Long userId;
    @TableField(value = "collection_name")
    private String collectionName;

}
