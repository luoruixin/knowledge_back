package com.ikun.knowledge_back.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ikun.knowledge_back.entity.Relation;
import com.ikun.knowledge_back.entity.Report;
import lombok.Data;

import java.util.List;


public interface RelationService extends IService<Relation> {
    public List<Relation> getRelationsByArticleId(Long articleId);
}
