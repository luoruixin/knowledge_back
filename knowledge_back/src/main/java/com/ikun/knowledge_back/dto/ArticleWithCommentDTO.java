package com.ikun.knowledge_back.dto;


import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
@Data
public class ArticleWithCommentDTO {

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

    private List<CommentDTO> articleCommentList;
}
