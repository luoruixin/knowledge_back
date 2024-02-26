package com.ikun.knowledge_back.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ikun.knowledge_back.entity.Collection;
import com.ikun.knowledge_back.entity.Feedback;
import com.ikun.knowledge_back.mapper.CollectionMapper;
import com.ikun.knowledge_back.mapper.FeedbackMapper;
import com.ikun.knowledge_back.service.CollectionService;
import com.ikun.knowledge_back.service.FeedbackService;
import org.springframework.stereotype.Service;

@Service
public class FeedbackServiceImpl extends ServiceImpl<FeedbackMapper, Feedback> implements FeedbackService {
}
