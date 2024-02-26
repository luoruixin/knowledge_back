package com.ikun.knowledge_back.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ikun.knowledge_back.common.R;
import com.ikun.knowledge_back.dto.ReportArticleDTO;
import com.ikun.knowledge_back.dto.ReportDetailDTO;
import com.ikun.knowledge_back.entity.Report;

import javax.servlet.http.HttpServletRequest;

import java.util.List;

public interface ReportService extends IService<Report> {
    R<Page> pageReportArticle(int page, int pageSize, HttpServletRequest request);

    R<String> handle(Long reportId);

    R<String> reportArticle(ReportArticleDTO reportArticleDTO, HttpServletRequest request);
    R<List<Report>> getOneArticleReport(long articleId);


    R<ReportDetailDTO> getReportDetail(Long reportId);

    R<String> handleReport(Long reportId, String result);
}
