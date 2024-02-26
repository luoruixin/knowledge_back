package com.ikun.knowledge_back.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ikun.knowledge_back.dto.PageReportArticleDTO;
import com.ikun.knowledge_back.entity.Report;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ReportMapper extends BaseMapper<Report> {
    @Select("select * from report inner join article a on report.article_id = a.article_id where report.report_state='未处理' limit #{page},#{pageSize}")
    public List<PageReportArticleDTO> getPageReportArticleDTO(int page,int pageSize);
}
