package com.ikun.knowledge_back.config;

import com.ikun.knowledge_back.common.JacksonObjectMapper;
import com.ikun.knowledge_back.interceptor.LoginInterceptor;
import com.ikun.knowledge_back.interceptor.RefreshTokenInterceptor;
import com.ikun.knowledge_back.interceptor.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

import javax.annotation.Resource;
import java.util.List;

@Configuration
@Slf4j
public class MvcConfig implements WebMvcConfigurer {
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    //TODO:完善拦截器
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //token刷新的拦截器，下面的拦截器会拦截所有请求,下面拦截器的order(0)表示优先级更高
        registry.addInterceptor(new RefreshTokenInterceptor(stringRedisTemplate)).addPathPatterns("/**").order(0);

        //登录拦截器
        registry.addInterceptor(new LoginInterceptor(stringRedisTemplate))
                .excludePathPatterns(
                        //这里是排除在外的，表示无需拦截的请求
                        "/search/article/searchByKeyword",    //通过关键词搜索无需拦截
                        "/search/article/suggestion",
                        "/search/article/insertOrUpdateES",
                        "/search/article/deleteES"
//                        下面这个是为了方便测试
//                        ,"/**"
                ).order(1);

//        //下面的几个拦截器是互斥的关系，为了提升性能
//        registry.addInterceptor(new HandleHousePageInterceptor()).addPathPatterns(
//                //家人只能查询房屋
//                "/house/page"
//
//        ).order(2);
//        registry.addInterceptor(new HandleHouseBindingInterceptor()).addPathPatterns(
//                "/house/selectByLevel",
//                "/house/bindHouse"
//        ).order(3);
//        registry.addInterceptor(new HandleResidentInterceptor()).addPathPatterns(
//                "/car/**",
//                "/problem/**",
//                "/complain/**",
//                "/file/**"
//        ).order(3);
//        registry.addInterceptor(new HandleOwnerInterceptor()).addPathPatterns(
//                "/house/delete",
//                "/parking/**",
//                "/familyRelationship/**",
//                "/vote/**"
//        ).order(4);
//        registry.addInterceptor(new HandleCommttieeInterceptor()).addPathPatterns(
//                "/CommitteeComplain/**",
//                "/committeeProblem/**",
//                "/committeeVote/**"
//        ).order(5);
    }

    /**
     * 扩展mvc框架的消息转换器(主要用于解决id的精度问题，这个转换器可以将Long型的id转为字符串)
     * @param converters
     */
    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        log.info("扩展消息转换器");
        //创建消息转换器对象
        MappingJackson2HttpMessageConverter messageConverter=new MappingJackson2HttpMessageConverter();
        //设置对象转换器，底层使用Jackson将Java对象转为json
        messageConverter.setObjectMapper(new JacksonObjectMapper());
        //将上面的消息转换器对象追加到mvc框架的转换器容器中(下标是0，优先使用)
        converters.add(0,messageConverter);
    }

    //添加解决跨域问题的配置
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    /**
     * Docket对象就是文档
     * @return
     */
    @Bean
    public Docket createRestApi() {
        // 文档类型
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.ikun.knowledge_back.controller"))//生成接口需要扫描controller包
                .paths(PathSelectors.any())
                .build();
    }

    //描述接口文档
    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("Ikun知识管理平台")
                .version("1.0")
                .description("知识管理平台接口文档")
                .build();
    }
}
