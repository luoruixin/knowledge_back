package com.ikun.knowledge_back.utils;

import cn.hutool.core.lang.UUID;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

public class DistributedRedisLock implements ILock{

    private String lockName; //锁的名称
    private StringRedisTemplate stringRedisTemplate;

    public DistributedRedisLock(String lockName, StringRedisTemplate stringRedisTemplate) {
        this.lockName = lockName;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    private static final String KEY_PREFIX="lock:";
    private static final String VALUE_ID_PREFIX= UUID.fastUUID().toString(true)+"-";
    private static final DefaultRedisScript<Long> UNLOCK_SCRIPT;//lua脚本
    static {
        UNLOCK_SCRIPT=new DefaultRedisScript<>();
        UNLOCK_SCRIPT.setLocation(new ClassPathResource("unlock.lua"));//配置lua脚本的位置
        UNLOCK_SCRIPT.setResultType(Long.class); //返回值类型
    }

    @Override
    public boolean tryLock(long timeoutSec) {
        String key=KEY_PREFIX+lockName;
        //获取线程标识(先用VALUE_ID_PREFIX区分不同的jvm，再用线程id区别同一个jvm中的不同线程)
        String threadId = VALUE_ID_PREFIX+Thread.currentThread().getId();
        //获取锁
        Boolean success = stringRedisTemplate.opsForValue().setIfAbsent(key, threadId, timeoutSec, TimeUnit.SECONDS);
        return Boolean.TRUE.equals(success);//自动拆箱会有空指针风险，这样写可以避免空指针的风险
    }

    @Override
    public void unlock() {

        //调用lua脚本
        stringRedisTemplate.execute(
                UNLOCK_SCRIPT,
                Collections.singletonList(KEY_PREFIX+lockName),
                VALUE_ID_PREFIX+Thread.currentThread().getId());
    }

//    @Override
//    public void unlock() {
//        //获取线程标识
//        String threadId=VALUE_ID_PREFIX+Thread.currentThread().getId();
//        //获取锁中的标识
//        String threadIdFromRedis = stringRedisTemplate.opsForValue().get(KEY_PREFIX + lockName);
//        ////_ 判断标示是否一 致
//        if(threadId.equals(threadIdFromRedis)){
//            //释放锁 （在这里有可能阻塞，所以我们要使用lua脚本保证操作的原子性）
//            stringRedisTemplate.delete(KEY_PREFIX+lockName);
//        }
//        //不一致就不用管
//    }
}
