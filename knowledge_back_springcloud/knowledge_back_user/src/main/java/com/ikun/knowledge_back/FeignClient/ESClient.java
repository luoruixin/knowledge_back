package com.ikun.knowledge_back.FeignClient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = "searchservice")
public interface ESClient {
    @PostMapping("/search/article/insertOrUpdateES")
    Boolean insertOrUpdateES(@RequestParam("articleDocJson") String articleDocJson);

    @DeleteMapping("/search/article/deleteES")
    Boolean deleteES(@RequestParam("articleId") Long articleId);
}
