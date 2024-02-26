package com.ikun.knowledge_back.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ikun.knowledge_back.dto.FeedbackWithUsernameDTO;
import com.ikun.knowledge_back.entity.Feedback;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface FeedbackMapper extends BaseMapper<Feedback> {

    @Select("SELECT u.name as userName,f.* " +
            "FROM feedback f " +
            "LEFT JOIN user u ON f.user_id = u.user_id " +
            "WHERE f.feedback_object = #{feedbackObject} AND f.feedback_state = 1")
    List<FeedbackWithUsernameDTO> getFeedbackWithUsernameByObject( long feedbackObject);

}

