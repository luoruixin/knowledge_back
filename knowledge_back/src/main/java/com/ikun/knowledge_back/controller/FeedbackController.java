package com.ikun.knowledge_back.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ikun.knowledge_back.common.R;
import com.ikun.knowledge_back.dto.CommentDTO;
import com.ikun.knowledge_back.dto.FeedbackDTO;
import com.ikun.knowledge_back.dto.FeedbackWithUsernameDTO;
import com.ikun.knowledge_back.service.FeedbackService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/feedback")
public class FeedbackController {

    @Autowired
     private FeedbackService feedbackService;

    //查看对平台的反馈


    //查看文章的反馈
    @GetMapping("/getArticleFeedback")
    public  R<List<FeedbackWithUsernameDTO>> getArticleFeedback(@RequestParam("feedbackObject") long feedbackObject ){
        return feedbackService.getMyFeedback(feedbackObject);
    }

    //反馈文章

    //反馈平台
    @PostMapping("/publishFeedback")
    public R<String> publishFeedback(@RequestBody FeedbackDTO feedbackDTO, HttpServletRequest request){
        return feedbackService.publishFeedback(feedbackDTO,request);//getMyArticle是查看自己的所有文章（发布、草稿...）
    }

    //删除某条反馈
@DeleteMapping("/deleteOneFeedback")
    public R<String>  deleteOneFeedback(@RequestParam("feedbackId") long feedbackId){
     return feedbackService.deleteOneFeedback(feedbackId);
}

    //    牛响加了一行注释进行测试
//    删除某篇文章（平台）的所有反馈
    @DeleteMapping("/deleteObjectFeedback")
    public R<String> deleteObjectFeedback(@RequestParam("feedbackObject") long feedbackObject){
      return feedbackService.deleteObjectFeedback(feedbackObject);
    }

    // n
    // xmx
    // lrx

}
