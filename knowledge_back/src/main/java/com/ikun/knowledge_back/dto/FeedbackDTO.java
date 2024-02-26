package com.ikun.knowledge_back.dto;


import lombok.Data;

//比Feedback多了一个userName
@Data
public class FeedbackDTO {

    private Long feedbackId;

    private Long userId;

    private String feedbackObject;


    private String feedbackContent;

    private String feedbackState;
}
