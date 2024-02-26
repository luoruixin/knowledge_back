package com.ikun.knowledge_back.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ikun.knowledge_back.entity.Report;
import com.ikun.knowledge_back.mapper.ReportMapper;
import com.ikun.knowledge_back.service.ReportService;
import org.springframework.stereotype.Service;

@Service
public class ReportServiceImpl extends ServiceImpl<ReportMapper, Report> implements ReportService {
}
