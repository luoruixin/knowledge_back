package com.ikun.knowledge_back.service.impl;

import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ikun.knowledge_back.common.Code;
import com.ikun.knowledge_back.common.CustomException;
import com.ikun.knowledge_back.common.R;
import com.ikun.knowledge_back.common.UserHolder;
import com.ikun.knowledge_back.dto.UserDTO;
import com.ikun.knowledge_back.entity.User;
import com.ikun.knowledge_back.mapper.UserMapper;
import com.ikun.knowledge_back.service.UserService;
import com.ikun.knowledge_back.utils.RegexUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.ikun.knowledge_back.utils.RedisConstants.LOGIN_USER_TTL;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
}
