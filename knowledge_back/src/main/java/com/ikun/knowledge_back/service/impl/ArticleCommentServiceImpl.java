package com.ikun.knowledge_back.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ikun.knowledge_back.common.R;
import com.ikun.knowledge_back.common.UserHolder;
import com.ikun.knowledge_back.dto.CommentDTO;
import com.ikun.knowledge_back.dto.FeedbackWithUsernameDTO;
import com.ikun.knowledge_back.entity.Article;
import com.ikun.knowledge_back.entity.ArticleComment;
import com.ikun.knowledge_back.mapper.ArticleCommentMapper;
import com.ikun.knowledge_back.service.ArticleCommentService;
import com.ikun.knowledge_back.service.ArticleService;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ArticleCommentServiceImpl extends ServiceImpl<ArticleCommentMapper, ArticleComment> implements ArticleCommentService {
    @Autowired
    private ArticleService articleService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    private final ArticleCommentMapper articleCommentMapper;

    @Autowired
    private RestHighLevelClient client;  //操纵es的

    @Autowired
    public ArticleCommentServiceImpl(ArticleCommentMapper articleCommentMapper) {
        this.articleCommentMapper = articleCommentMapper;
    }

    @Transactional
    @Override
    public R<String> publishComment(CommentDTO commentDTO, HttpServletRequest request) {
        Long articleId = commentDTO.getArticleId();
        String commentContent = commentDTO.getCommentContent();
        if (articleId == null || StrUtil.isEmpty(commentContent)) {
            return R.error("请将信息填充完整");
        }
        ArticleComment articleComment = new ArticleComment();
        articleComment.setCommentContent(commentContent);
        articleComment.setArticleId(articleId);
        articleComment.setUserId(UserHolder.getCurrentId(request, stringRedisTemplate));
        articleComment.setCommentTime(LocalDateTime.now());
        articleComment.setCommentState("发布");
        save(articleComment);

        //评论数+1
        Article article = articleService.getById(articleId);
        article.setCommentCount(article.getCommentCount() + 1);
        articleService.updateById(article);

        //更新es
        UpdateRequest esRequest = new UpdateRequest("article", articleId.toString());
        //准备参数
        esRequest.doc(
                "commentCount",article.getCommentCount()
        );
        //2.发送请求
        try {
            client.update(esRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return R.success("发布成功");
    }

    @Override
    public R<List<ArticleComment>> getAllComment(HttpServletRequest request) {
        LambdaQueryWrapper<ArticleComment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ArticleComment::getUserId, UserHolder.getCurrentId(request, stringRedisTemplate));
        List<ArticleComment> articleLikes = list(queryWrapper);
        return R.success(articleLikes);
    }

    public R<List<CommentDTO>> getComment(long articleId) {
        List<CommentDTO> CommentList = articleCommentMapper.getCommentDTO(articleId);
        return R.success(CommentList);
    }
}
