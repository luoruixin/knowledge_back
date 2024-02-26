package com.ikun.knowledge_back.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLIntegrityConstraintViolationException;

/**
 * 全局异常处理(捕获)
 */
//TODO:该类中最后加上错误日志
@ControllerAdvice(annotations = {RestController.class, Controller.class})   //告诉springboot处理头上加了@RestController的controller中的异常
@ResponseBody
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 异常处理方法（新增员工或菜品时重复的问题）
     * @return
     */
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)  //告诉springboot这是处理什么异常的方法
    public R<String> exceptionHandler(SQLIntegrityConstraintViolationException ex){
        log.error(ex.getMessage());

        if(ex.getMessage().contains("Duplicate entry")){
            String[] split = ex.getMessage().split(" ");
            String msg = split[2]+"已存在";
            return R.error(msg);
        }

        return R.error("未知错误");
    }

    /**
     * 异常处理方法(捕获自定义异常)
     * @return
     */
    @ExceptionHandler(CustomException.class)  //告诉springboot这是处理什么异常的方法
    public R<String> exceptionHandler(CustomException ex){
        log.error(ex.getMessage());

        return R.error(ex.getMessage());
    }

    /**
     * 异常处理方法(捕获全局异常)
     * @return
     */
    @ExceptionHandler(Exception.class)  //告诉springboot这是处理什么异常的方法
    public R<String> exceptionHandler(Exception ex){
        log.error(ex.getMessage());

        return R.error("网络问题，请重试");
    }
}
