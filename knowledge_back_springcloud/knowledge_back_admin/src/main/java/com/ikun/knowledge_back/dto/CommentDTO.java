package com.ikun.knowledge_back.dto;

import lombok.Data;

import java.time.LocalDateTime;

//与articleComment相比多了username
@Data
public class CommentDTO {
    private Long articleCommentId;
    private Long articleId;
    private Long userId;
    private String userName;
    private LocalDateTime commentTime;
    private String commentContent;
    private String commentState;
}
