package com.ikun.knowledge_back.controller;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ikun.knowledge_back.common.R;
import com.ikun.knowledge_back.dto.HandleReportDTO;
import com.ikun.knowledge_back.dto.ReportArticleDTO;
import com.ikun.knowledge_back.dto.ReportDetailDTO;
import com.ikun.knowledge_back.entity.Report;
import com.ikun.knowledge_back.service.ReportService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.support.SimpleTriggerContext;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.util.List;


@Slf4j
@RestController
@RequestMapping("/report")
public class ReportController{
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private ReportService reportService;

    //管理员分页查看被举报的文章
    @GetMapping("/pageReportArticle")
    public R<Page> pageReportArticle(int page, int pageSize, HttpServletRequest request){
        return reportService.pageReportArticle(page,pageSize,request);
    }

    //处理(将状态修改一下)
    @PutMapping("/handle")
    public R<String> handle(Long reportId){
        return reportService.handle(reportId);
    }

    //举报文章(+事务)
    @PostMapping("/reportArticle")
    public R<String> reportArticle(@RequestBody ReportArticleDTO reportArticleDTO,HttpServletRequest request){
        return reportService.reportArticle(reportArticleDTO,request);
    }

    //查看举报详情
    @GetMapping("/getReportDetail")
    public R<ReportDetailDTO> getReportDetail(Long reportId ,HttpServletRequest request){
        // 查询 举报
        return reportService.getReportDetail(reportId);
    }

    //查看某篇文章的举报信息
    @GetMapping("/getOneArticleReport")
    public R<List<Report>> getOneArticleReport(@RequestParam("articleId") long articleId){
        return reportService.getOneArticleReport(articleId);

    }

    @PostMapping("/handleReport")
    public R<String> handleReport(@RequestBody HandleReportDTO handleReportDTO){
        return reportService.handleReport(handleReportDTO.getReportId(), handleReportDTO.getResult());
    }
}
