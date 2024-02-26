package com.ikun.knowledge_back.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.time.LocalDateTime;

//与article相比多了name
@Data
public class ArticleCheckDTO {
    private Long articleId;
    private String title;
    private Long userId;
    private int likeCount;
    private int commentCount;
    private int collectCount;
    private int scanCount;
    private String articleClass;
    private String articleContent;
    private LocalDateTime articleTime;
    private String articleState;
    private String name;
}
