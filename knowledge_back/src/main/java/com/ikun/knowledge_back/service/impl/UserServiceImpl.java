package com.ikun.knowledge_back.service.impl;

import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ikun.knowledge_back.common.Code;
import com.ikun.knowledge_back.common.CustomException;
import com.ikun.knowledge_back.common.R;
import com.ikun.knowledge_back.common.UserHolder;
import com.ikun.knowledge_back.dto.AdminData4DTO;
import com.ikun.knowledge_back.dto.LoginFormDTO;
import com.ikun.knowledge_back.dto.RegisterFormDTO;
import com.ikun.knowledge_back.dto.UserDTO;
import com.ikun.knowledge_back.entity.Article;
import com.ikun.knowledge_back.entity.Feedback;
import com.ikun.knowledge_back.entity.User;
import com.ikun.knowledge_back.mapper.UserMapper;
import com.ikun.knowledge_back.service.ArticleService;
import com.ikun.knowledge_back.service.FeedbackService;
import com.ikun.knowledge_back.service.UserService;
import com.ikun.knowledge_back.utils.RegexUtils;
import org.slf4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.ikun.knowledge_back.utils.RedisConstants.LOGIN_USER_TTL;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
//    @Autowired
//    private Logger logger;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private ArticleService articleService;
    @Autowired
    private FeedbackService feedbackService;
    @Resource
    private JavaMailSender javaMailSender;
    public boolean sendVerificationCode(String mail, String code) {

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(mail); // 设置收件人的电子邮件地址
        message.setSubject("验证码"); // 设置邮件主题
        message.setText("您的验证码是：" + code); // 设置邮件正文内容
        message.setFrom("2531768325@qq.com");
        javaMailSender.send(message); // 发送邮件
        return true;
    }
    @Override
    public R<String> sendCode(String phoneOrEmail, HttpServletRequest request) {
        if(StrUtil.isEmpty(phoneOrEmail)){
            throw new CustomException("手机号或邮箱不能为空");
        }
        //TODO:此处记得打开
//        if(!phoneOrEmailTrue){
//            //2.如果不符合，返回错误信息
//            return Result.fail("手机号格式错误");
//        }

        // 3. 符合，生成验证码
        String code = new StringBuilder().append(RandomUtil.randomInt(1, 10)).append(RandomUtil.randomNumbers(5)).toString();
//        // 4.保存验证码到session
//        session.setAttribute("code",code);

        //根据手机号或者邮箱判断数据库中是否已经存在用户
        if(RegexUtils.isEmailTrue(phoneOrEmail)){
            if(sendVerificationCode(phoneOrEmail,code)){
                stringRedisTemplate.opsForValue().set("code:"+phoneOrEmail,code,2, TimeUnit.MINUTES);//有效期是两分钟
                return R.success("发送成功");
            }else return R.error("发送失败");
        }else if(RegexUtils.isPhoneTrue(phoneOrEmail)){
            return R.success("发送成功");
        }else {
            return R.error("请输入正确的手机号和邮箱");
        }

    }

    @Override
    public R<String> register(RegisterFormDTO registerForm, HttpServletRequest request) {
        if(StrUtil.isEmpty(registerForm.getName())
                ||StrUtil.isEmpty(registerForm.getPhoneOrEmail())
                ||StrUtil.isEmpty(registerForm.getCode())
                ||StrUtil.isEmpty(registerForm.getPassword())){
            return R.error("信息请填充完整");
        }
        String phoneOrEmail = registerForm.getPhoneOrEmail();
        String code = registerForm.getCode();
        String name= registerForm.getName();
        //先检验验证码是否正确
        if(!verificateCode(phoneOrEmail,code)){ //这里采用反向校验
            //3.不一致，报错
            return R.error("验证码错误");
        }

        if(RegexUtils.isEmailTrue(phoneOrEmail)){
            //如果输入的是邮箱
            User user = query().eq("email", phoneOrEmail).one();

            if(user!=null){
                return R.error("该邮箱已被绑定");
            }else {
                // 不存在,注册
                user=new User();
                user.setEmail(phoneOrEmail);
                user.setName(name);
                //注意下面前端要md5加密
                user.setPassword(registerForm.getPassword());
                user.setUserState("用户");
                user.setAvatar("0");
                save(user);
                return R.success("注册成功");
            }
        }else if(RegexUtils.isPhoneTrue(phoneOrEmail)){
            //如果输入的是邮箱
            User user = query().eq("phone", phoneOrEmail).one();
            if(user!=null){
                return R.error("该手机号已被绑定");
            }else {
                // 不存在,注册
                user = new User();
                user.setPhone(phoneOrEmail);
                user.setName(name);
                //注意下面前端要md5加密
                user.setPassword(registerForm.getPassword());
                user.setUserState("用户");
                save(user);
                return R.success("注册成功");
            }
        }else {
            return R.error("请输入正确的手机号和邮箱");
        }
    }

    @Override
    public R<String> loginByCode(LoginFormDTO loginFormDTO, HttpServletRequest request) {
        String phoneOrEmail = loginFormDTO.getPhoneOrEmail();
        String code = loginFormDTO.getCode();
        if(StrUtil.isEmpty(phoneOrEmail)||StrUtil.isEmpty(code)){
            return R.error("请将信息填充完整");
        }
        User user=null;
        //根据手机号或者邮箱判断数据库中是否已经存在用户
        if(RegexUtils.isEmailTrue(phoneOrEmail)){
            user = query().eq("email", phoneOrEmail).one();
        }else if(RegexUtils.isPhoneTrue(phoneOrEmail)){
            user = query().eq("phone", phoneOrEmail).one();
        }else {
            return R.error("请输入正确的手机号和邮箱");
        }
        //先检验验证码是否正确
        if(!verificateCode(phoneOrEmail,code)){ //这里采用反向校验
            //3.不一致，报错
            return R.error("验证码错误");
        }

        //5.判断用户是否存在
        if(user==null){
            // 6.不存在
            return R.error("该用户不存在，请注册");
        }
        //7.存在，用户信息到redis
        UserDTO userDTO=new UserDTO();
        //7.1随机生成token，作为登录令牌(token不能使用手机号，不安全)
        String token = UUID.randomUUID().toString(true);
        //7.2将User对象转为HashMap存储
        BeanUtils.copyProperties(user,userDTO);
        Map<String, String> userMap=new HashMap<>();
        userToMap(userDTO, userMap);

        // 7.3.存储
        stringRedisTemplate.opsForHash().putAll("login:token:"+token,userMap);
        //7.4设置有效期
        stringRedisTemplate.expire("login:token:"+token,LOGIN_USER_TTL,TimeUnit.MINUTES);
        // 8. 返回token
        //这里将token返回给前端后，前端会自动缓存起来
        if ("管理员".equals(user.getUserState())){
            return R.success(token, Code.administratorLogin);
        }else {
            return R.success(token,Code.userLogin);
        }
    }

    @Override
    public R<String> loginByPwd(LoginFormDTO loginFormDTO, HttpServletRequest request) {
        String phoneOrEmail = loginFormDTO.getPhoneOrEmail();
        String password = loginFormDTO.getPassword();
        if(StrUtil.isEmpty(phoneOrEmail)||StrUtil.isEmpty(password)){
            return R.error("请将信息填充完整");
        }

        User user=null;

        //根据手机号或者邮箱判断数据库中是否已经存在用户
        if(RegexUtils.isEmailTrue(phoneOrEmail)){
            user = query().eq("email", phoneOrEmail).one();
        }else if(RegexUtils.isPhoneTrue(phoneOrEmail)){
            user = query().eq("phone", phoneOrEmail).one();
        }else {
            return R.error("请输入正确的手机号和邮箱");
        }

        //5.判断用户是否存在
        if(user==null){
            // 6.不存在
            return R.error("该用户不存在，请注册");
        }
        //判断密码是否正确(加密后再对比)
        if (!password.equals(user.getPassword())) {
            return R.error("密码不正确");
        }
        //7.存在，用户信息到redis
        UserDTO userDTO=new UserDTO();
        //7.1随机生成token，作为登录令牌(token不能使用手机号，不安全)
        String token = UUID.randomUUID().toString(true);
        //7.2将User对象转为HashMap存储
        BeanUtils.copyProperties(user,userDTO);
        Map<String, String> userMap=new HashMap<>();
        userToMap(userDTO, userMap);

        // 7.3.存储
        stringRedisTemplate.opsForHash().putAll("login:token:"+token,userMap);
        //7.4设置有效期
        stringRedisTemplate.expire("login:token:"+token,LOGIN_USER_TTL,TimeUnit.MINUTES);
        // 8. 返回token
        //这里将token返回给前端后，前端会自动缓存起来
        if ("管理员".equals(user.getUserState())){
            return R.success(token,Code.administratorLogin);
        }else {
            return R.success(token,Code.userLogin);
        }
    }

    @Override
    public R<String> foundPwd(RegisterFormDTO registerForm, HttpServletRequest request) {
        String phoneOrEmail=registerForm.getPhoneOrEmail();
        String code=registerForm.getCode();
        String password=registerForm.getPassword();
        if(StrUtil.isEmpty(phoneOrEmail)
                ||StrUtil.isEmpty(code)||StrUtil.isEmpty(password)){
            return R.error("请将信息填充完整");
        }
        //先检验验证码是否正确
        if(!verificateCode(phoneOrEmail,code)){ //这里采用反向校验
            //3.不一致，报错
            return R.error("验证码错误");
        }
        User user=null;
        //根据手机号或者邮箱判断数据库中是否已经存在用户
        if(RegexUtils.isEmailTrue(phoneOrEmail)){
            //如果输入的是邮箱
            user = query().eq("email", phoneOrEmail).one();
        }else if(RegexUtils.isPhoneTrue(phoneOrEmail)){
            //如果输入的是手机号
            user = query().eq("phone", phoneOrEmail).one();
        }else {
            return R.error("请输入正确的手机号和邮箱");
        }

        if(user==null){
            return R.error("该用户不存在，请注册");
        }else {
            // 如果存在，则修改密码
            //注意下面前端要md5加密
            user.setPassword(password);
            updateById(user);
            return R.success("修改密码成功");
        }
    }

    @Override
    public R<String> updateInformation(User user, HttpServletRequest request) {
        try {
            // 更新用户信息
            updateById(user);
            return R.success("修改成功");
        } catch (Exception e) {
            e.printStackTrace();
            return R.error("An error occurred during updating user information.");
        }
    }
    @Override
    public R<String> updateAvatar(User user, HttpServletRequest request) {
        try {
            // 根据用户ID查询用户信息
            User existingUser = getById(user.getUserId());
            if (existingUser == null) {
                return R.error("User not found");
            }
            // 更新头像信息
            existingUser.setAvatar(user.getAvatar());
            // 执行更新操作
            updateById(existingUser);
            return R.success("Avatar updated successfully");
        } catch (Exception e) {
            e.printStackTrace();
            return R.error("An error occurred during updating avatar.");
        }
    }
    @Override
    public R<String> updateName(User user, HttpServletRequest request) {
        try {
            // 根据用户ID查询用户信息
            User existingUser = getById(user.getUserId());
            if (existingUser == null) {
                return R.error("User not found");
            }
            // 更新头像信息
            existingUser.setName(user.getName());
            // 执行更新操作
            updateById(existingUser);
            return R.success("Avatar updated successfully");
        } catch (Exception e) {
            e.printStackTrace();
            return R.error("An error occurred during updating avatar.");
        }
    }
    @Override
    public R<String> updateAge(User user, HttpServletRequest request) {
        try {
            // 根据用户ID查询用户信息
            User existingUser = getById(user.getUserId());
            if (existingUser == null) {
                return R.error("User not found");
            }
            // 更新头像信息
            existingUser.setAge(user.getAge());
            // 执行更新操作
            updateById(existingUser);
            return R.success("Avatar updated successfully");
        } catch (Exception e) {
            e.printStackTrace();
            return R.error("An error occurred during updating avatar.");
        }
    }
    @Override
    public R<String> updateSex(User user, HttpServletRequest request) {
        try {
            // 根据用户ID查询用户信息
            User existingUser = getById(user.getUserId());
            if (existingUser == null) {
                return R.error("User not found");
            }
            // 更新头像信息
            existingUser.setSex(user.getSex());
            // 执行更新操作
            updateById(existingUser);
            return R.success("Avatar updated successfully");
        } catch (Exception e) {
            e.printStackTrace();
            return R.error("An error occurred during updating avatar.");
        }
    }

    @Override
    public R<AdminData4DTO> adminData4(HttpServletRequest request) {
        AdminData4DTO adminData4DTO=new AdminData4DTO();
        //审核中
        LambdaQueryWrapper<Article> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(Article::getArticleState,"待审核");
        adminData4DTO.setChecking(articleService.count(queryWrapper));

        //审核完
        LambdaQueryWrapper<Article> queryWrapper1=new LambdaQueryWrapper<>();
        queryWrapper1.eq(Article::getArticleState,"已发布").or().eq(Article::getArticleState,"已退回");
        adminData4DTO.setChecked(articleService.count(queryWrapper1));

        //举报中
        LambdaQueryWrapper<Article> queryWrapper2=new LambdaQueryWrapper<>();
        queryWrapper2.eq(Article::getArticleState,"被举报");
        adminData4DTO.setReport(articleService.count(queryWrapper2));

        //
        LambdaQueryWrapper<Feedback> queryWrapper3=new LambdaQueryWrapper<>();
        queryWrapper3.eq(Feedback::getFeedbackState,"未处理");
        adminData4DTO.setFeedback(feedbackService.count(queryWrapper3));

        return R.success(adminData4DTO);
    }
    //-----------------------------------工具类-----------------------------------------------

    private boolean verificateCode(String phoneOrEmail,String code){

        boolean codeTrue = RegexUtils.isCodeTrue(code);
        if(!codeTrue){
            //2.如果不符合，返回错误信息
            return false;
        }
        //2.校验验证码  从redis中获取
        String cacheCode=stringRedisTemplate.opsForValue().get("code:"+phoneOrEmail);
        //这是前端传过来的code
        //这里采用反向校验
        //3.不一致，报错
        return cacheCode != null && cacheCode.equals(code);
    }


    //将userDto映射到map的工具类
    private static void userToMap(UserDTO userDTO, Map<String, String> userMap) {
        userMap.put("userId", userDTO.getUserId().toString());
        userMap.put("name", userDTO.getName());
        userMap.put("email", String.valueOf(userDTO.getEmail()));
        userMap.put("phone", String.valueOf(userDTO.getPhone()));
        userMap.put("sex",String.valueOf(userDTO.getSex()));
        userMap.put("age",String.valueOf(userDTO.getAge()));
        userMap.put("recommendation",String.valueOf(userDTO.getRecommendation()));
        userMap.put("userState",String.valueOf(userDTO.getUserState()));
        userMap.put("avatar",String.valueOf(userDTO.getAvatar()));
    }
}
