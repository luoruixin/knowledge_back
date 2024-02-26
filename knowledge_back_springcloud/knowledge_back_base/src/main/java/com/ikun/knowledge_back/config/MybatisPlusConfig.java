package com.ikun.knowledge_back.config;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 配置mp的分页插件(分页拦截器)
 */
@Configuration
public class MybatisPlusConfig {

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor(){
        //1.定义mp拦截器
        MybatisPlusInterceptor mybatisPlusInterceptor=new MybatisPlusInterceptor();
        //2.添加具体的拦截器（分页拦截器PaginationInnerInterceptor）
        mybatisPlusInterceptor.addInnerInterceptor(new PaginationInnerInterceptor());
        return mybatisPlusInterceptor;
    }
}
