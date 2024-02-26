package com.ikun.knowledge_back.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ikun.knowledge_back.common.R;
import com.ikun.knowledge_back.common.UserHolder;
import com.ikun.knowledge_back.dto.FeedbackDTO;
import com.ikun.knowledge_back.dto.FeedbackWithUsernameDTO;
import com.ikun.knowledge_back.entity.Article;
import com.ikun.knowledge_back.entity.ArticleComment;
import com.ikun.knowledge_back.entity.Feedback;
import com.ikun.knowledge_back.mapper.FeedbackMapper;
import com.ikun.knowledge_back.service.FeedbackService;
import org.elasticsearch.action.update.UpdateRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class FeedbackServiceImpl extends ServiceImpl<FeedbackMapper, Feedback> implements FeedbackService {
    @Resource
    private StringRedisTemplate stringRedisTemplate; //操纵redis的
    private final FeedbackMapper feedbackMapper;
      @Autowired
    public FeedbackServiceImpl(FeedbackMapper feedbackMapper) {
        this.feedbackMapper = feedbackMapper;
    }

    @Override
    public R<List<FeedbackWithUsernameDTO>> getMyFeedback(long feedbackObject){

        List<FeedbackWithUsernameDTO> feedbackList = feedbackMapper.getFeedbackWithUsernameByObject(feedbackObject);
        return R.success(feedbackList);
    }

    @Override
    public  R<String> deleteOneFeedback(long feedbackId){

        UpdateWrapper<Feedback> updateWrapper = new UpdateWrapper<>();
        updateWrapper.set("feedback_state", 0)
                .eq("feedback_id", feedbackId);

        // 执行逻辑删除操作
        feedbackMapper.update(null, updateWrapper);
        return null;
    }

    @Override
    public R<String> deleteObjectFeedback(long feedbackObject) {
        UpdateWrapper<Feedback> updateWrapper = new UpdateWrapper<>();
        updateWrapper.set("feedback_state", 0)
                .eq("feedback_object", feedbackObject);

        // 执行逻辑删除操作
        feedbackMapper.update(null, updateWrapper);
        return null;
    }

    @Override
    public R<String> publishFeedback(FeedbackDTO feedbackDTO, HttpServletRequest request){
        Long userid = UserHolder.getCurrentId(request, stringRedisTemplate);
        if (userid== null){
            return R.error("请登录");
        }
        String feedbackContent = feedbackDTO.getFeedbackContent();
        if (StrUtil.isEmpty(feedbackContent)) {
            return R.error("请将信息填充完整");
        }
        Feedback feedback = new Feedback();
        feedback.setFeedbackObject(feedbackDTO.getFeedbackObject());
        feedback.setFeedbackContent(feedbackContent);
        feedback.setUserId(UserHolder.getCurrentId(request, stringRedisTemplate));
        feedback.setFeedbackState("1");
        save(feedback);

        return R.success("发布成功");
    }
}
