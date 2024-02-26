package com.ikun.knowledge_back.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.ikun.knowledge_back.entity.Article;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class PageReportArticleDTO implements Serializable {
    private Long reportId;
    private Long articleId;
    private String title;
    private String articleClass;
    private LocalDateTime articleTime;
    private String articleState;

    private Long userId;
    private String reportClass;
    private String reportContent;
    private String reportState;
}
