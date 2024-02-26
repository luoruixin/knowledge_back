package com.ikun.knowledge_back.dto;

import lombok.Data;

//搜索框所需属性
@Data
public class SearchFromDTO {
    private String key;
    private Integer page;
    private Integer size;
    private String sortBy;
    private String articleClass;
}
