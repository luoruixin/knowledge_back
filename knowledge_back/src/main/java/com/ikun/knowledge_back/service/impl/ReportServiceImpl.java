package com.ikun.knowledge_back.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ikun.knowledge_back.common.CustomException;
import com.ikun.knowledge_back.common.R;
import com.ikun.knowledge_back.common.UserHolder;
import com.ikun.knowledge_back.dto.*;
import com.ikun.knowledge_back.entity.*;
import com.ikun.knowledge_back.mapper.ReportMapper;
import com.ikun.knowledge_back.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import java.util.Objects;

@Service
public class ReportServiceImpl extends ServiceImpl<ReportMapper, Report> implements ReportService {
    @Autowired
    private ReportMapper reportMapper;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private ArticleService articleService;
    @Autowired
    private UserService userService;
    @Autowired
    private RelationService relationService;
    @Autowired
    private ArticleTagService articleTagService;

    @Value("${knowledge_back.articleFolderPath}")
    private String articleFolderPath;


    @Override
    public R<Page> pageReportArticle(int page, int pageSize, HttpServletRequest request) {
        Page<PageReportArticleDTO> pageInfo=new Page<>(page,pageSize);
        List<PageReportArticleDTO> pageReportArticleDTOs = reportMapper.getPageReportArticleDTO(page-1, pageSize);
        pageInfo.setRecords(pageReportArticleDTOs);
        pageInfo.setTotal(pageReportArticleDTOs.size());
        return R.success(pageInfo);
    }

    @Override
    public R<String> handle(Long reportId) {
        if(reportId==null){
            return R.error("id为空");
        }
        Report report = getById(reportId);
        report.setReportState("已处理");
        updateById(report);
        return R.success("处理成功");
    }

    @Override
    public R<String> reportArticle(ReportArticleDTO reportArticleDTO, HttpServletRequest request) {
        String articleContent = reportArticleDTO.getArticleContent();
        Long articleId = reportArticleDTO.getArticleId();
        String reportClass = reportArticleDTO.getReportClass();
        if(articleId==null|| StrUtil.isEmpty(articleContent)){
            return R.error("信息不完整");
        }
        Report report=new Report();
        report.setArticleId(articleId);
        report.setReportContent(articleContent);
        report.setReportClass(reportClass);
        report.setUserId(UserHolder.getCurrentId(request,stringRedisTemplate));
        report.setReportState("未处理");
        save(report);
        return R.success("举报成功");
    }
    @Override
    public R<List<Report>> getOneArticleReport(long articleId) {


        QueryWrapper<Report> queryWrapper =new QueryWrapper<>();
        queryWrapper.eq("article_id",articleId);
        List<Report> list=reportMapper.selectList(queryWrapper);
        return R.success(list);
    }

    @Override
    public R<ReportDetailDTO> getReportDetail(Long reportId) {
        // 1. 管理员身份校验

        // 新建DTO
        ReportDetailDTO reportDetailDTO = new ReportDetailDTO();
        // 2. 通过 reportId 查询 Report
        Report report = getById(reportId);


        // 3. 获取文章Id
        Long articleId = report.getArticleId();

//        private Article article;
//        private User author;
//        private List<RelationArticleDTO> relationArticles;
//        private List<ArticleTag> articleTagList;
//        ArticleService articleService = new ArticleServiceImpl();

        // 没有校验文章
        Article article = articleService.getById(articleId);

        String articlePath = articleFolderPath+article.getArticleContent();//替换articleContent
        String content=null;
        try {
            content = FileUtil.readString(articlePath, Charset.defaultCharset());
        }catch (Exception e){
            System.out.println(e);
            throw new CustomException("文章未找到");
        }
        article.setArticleContent(content);

        //获取作者
//        UserService userService = new UserServiceImpl();
        User author = userService.getById(article.getUserId());
        author.setPassword(null);  //设置密码不可见

        //获取关联文章
        //获取关联文章
//        RelationService relationService = new RelationServiceImpl();
        List<RelationArticleDTO> relationArticles = new ArrayList<>();
        List<Relation> relationList = relationService.getRelationsByArticleId(articleId);
        for(Relation relation: relationList){
            Long relationId = relation.getRelationId();
            Long relatedArticleId = relation.getRelatedArticleId();
            Article relatedArticle = articleService.getById(relatedArticleId);
            String relatedArticleTitle = relatedArticle.getTitle();

            RelationArticleDTO relationArticleDTO = new RelationArticleDTO();
            relationArticleDTO.setRelationArticleId(relationId);
            relationArticleDTO.setRelatedArticleId(relatedArticleId);
            relationArticleDTO.setRelatedArticleTitle(relatedArticleTitle);

            relationArticles.add(relationArticleDTO);
        }

        //获取文章标签
//        ArticleTagService articleTagService = new ArticleTagServiceImpl();
        QueryWrapper<ArticleTag> articleTagQueryWrapper = new QueryWrapper<>();
        articleTagQueryWrapper.eq("article_id",articleId);
        List<ArticleTag> articleTagList = articleTagService.list(articleTagQueryWrapper);

        reportDetailDTO.setReport(report);
        reportDetailDTO.setArticle(article);
        reportDetailDTO.setAuthor(author);
        reportDetailDTO.setRelationArticles(relationArticles);
        reportDetailDTO.setArticleTagList(articleTagList);

        return R.success(reportDetailDTO);
    }

    @Override
    public R<String> handleReport(Long reportId, String result) {
//        Report report = getById(Long.parseLong(reportId));
        Report report = getById(reportId);
        if (report == null) {
            // 处理报告为空的情况，可能返回错误响应
            return R.error("找不到id为：" + reportId + "的报告");
        }

        // 将Report改为已处理
        UpdateWrapper<Report> reportUpdateWrapper = new UpdateWrapper<>();
        reportUpdateWrapper.eq("report_id", reportId);
        reportUpdateWrapper.set("report_state", "已处理");
        baseMapper.update(null, reportUpdateWrapper);

        // 根据结果判定文章状态
        Long articleId = report.getArticleId();


        if (Objects.equals(result, "ignore")) {
            return R.success("忽略成功");
        } else if (Objects.equals(result, "remove")) {
            return articleService.changeReportArticleState(articleId, "已退回");
        }else {
            return R.success("失败");
        }
    }
}
