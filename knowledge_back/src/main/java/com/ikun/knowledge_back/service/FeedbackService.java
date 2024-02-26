package com.ikun.knowledge_back.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ikun.knowledge_back.common.R;
import com.ikun.knowledge_back.dto.FeedbackDTO;
import com.ikun.knowledge_back.dto.FeedbackWithUsernameDTO;
import com.ikun.knowledge_back.entity.Feedback;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface FeedbackService extends IService<Feedback> {

    R<List<FeedbackWithUsernameDTO>> getMyFeedback(long feedbackObject);

    R<String> deleteOneFeedback(long feedbackId);

    R<String> deleteObjectFeedback(long feedbackObject);

    R<String> publishFeedback(FeedbackDTO feedbackDTO, HttpServletRequest request);
}
