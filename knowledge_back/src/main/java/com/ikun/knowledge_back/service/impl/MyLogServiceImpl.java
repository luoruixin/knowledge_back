package com.ikun.knowledge_back.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ikun.knowledge_back.entity.MyLog;
import com.ikun.knowledge_back.mapper.MyLogMapper;
import com.ikun.knowledge_back.service.MyLogService;
import org.springframework.stereotype.Service;

@Service
public class MyLogServiceImpl extends ServiceImpl<MyLogMapper, MyLog> implements MyLogService {
}
