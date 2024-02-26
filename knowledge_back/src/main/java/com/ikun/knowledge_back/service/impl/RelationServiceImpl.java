package com.ikun.knowledge_back.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ikun.knowledge_back.entity.Relation;
import com.ikun.knowledge_back.entity.Report;
import com.ikun.knowledge_back.mapper.RelationMapper;
import com.ikun.knowledge_back.mapper.ReportMapper;
import com.ikun.knowledge_back.service.RelationService;
import com.ikun.knowledge_back.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RelationServiceImpl  extends ServiceImpl<RelationMapper, Relation> implements RelationService {
    @Autowired
    private RelationMapper relationMapper;

    @Override
    public List<Relation> getRelationsByArticleId(Long articleId) {
        QueryWrapper<Relation> wrapper = new QueryWrapper<>();
        wrapper.eq("article_id", articleId);
        return relationMapper.selectList(wrapper);
    }
}
