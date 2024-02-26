package com.ikun.knowledge_back.utils;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

//封装缓存常用方法
@Slf4j
@Component
public class CacheClient {
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 方法1：将任意Java对象序列化为json并存储在string类型的key中，并且可以设置TTL过期时间
     * 方法2：将任意Java对象序列化为json并存储在string类型的key中，并且可以设置逻辑过期时间，用于处理缓存击穿问题
     * 方法3：根据指定的ky查询缓存，并反序列化为指定类型，利用缓存空值的方式解决缓存穿透问题
     * 方法4：根据指定的ky查询缓存，并反序列化为指定类型，需要利用逻辑过期解决缓存击穿问题
     */

    //方法1：将任意Java对象序列化为json并存储在string类型的key中，并且可以设置TTL过期时间
    public void set(String key, Object value, Long time, TimeUnit unit){
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(value),time,unit);
    }

    //方法2：将任意Java对象序列化为json并存储在string类型的key中，并且可以设置逻辑过期时间，用于处理缓存击穿问题
    public void setWithLogicalExpire(String key, Object value, Long time, TimeUnit unit){
        //设置逻辑过期
        RedisData redisData = new RedisData();
        redisData.setExpireTime(LocalDateTime.now().plusSeconds(unit.toSeconds(time))); //注意要将单位转为秒
        redisData.setData(value);
        //2.写入redis
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(redisData));
    }

    /**
     *  方法3：根据指定的key查询缓存，并反序列化为指定类型，利用缓存空值的方式解决缓存穿透问题
     * @param keyPrefix key的前缀
     * @param id
     * @param type
     * @return
     * @param <R>
     * @param <ID>
     *      Function<ID,R> queryDB的意思是方法名为queryDB,传入参数为ID,返回值类型为R
     */
    public <R,ID> R handlePassThrough(
            String keyPrefix, ID id, Class<R> type, Function<ID,R> queryDB,Long time, TimeUnit unit){
        // 1. 从redis查询商铺缓存
        String key=keyPrefix + id;
        String json = stringRedisTemplate.opsForValue().get(key);
        //2.判断缓存redis中是否存在----------------------------------
        if(StrUtil.isNotBlank(json)){
            // 3.存在，直接返回
            return JSONUtil.toBean(json, type);
        }
        //判断命中的是否是""
        if(json!=null){ //这里等价于 "".equals(json)
            //返回一个错误信息
            return null;
        }
        R r=queryDB.apply(id);

        // 5.判断数据库中是否存在---------------------------------------
        if(r==null){
            //不存在则要避免缓存穿透问题
            //将空值写入redis(避免缓存穿透)
            stringRedisTemplate.opsForValue().set(key,"",1,TimeUnit.MINUTES);//此时的有效期要设置得比较短(防止恶意请求)
            //返回错误信息
            return null;
        }
        // 6.数据库中存在，写入redis（前提是redis缓存中不存在）
        stringRedisTemplate.opsForValue().set("cache:shop:" + id,JSONUtil.toJsonStr(r),time,unit);//过期时间是30分钟
        //7.返回
        return r;
    }

    //线程池
    private static final ExecutorService CACHE_REBUILD_THREADPOOLS= Executors.newFixedThreadPool(10);

    /**
     * 方法4：根据指定的ky查询缓存，并反序列化为指定类型，需要利用逻辑过期解决缓存击穿问题
     * @param id
     * @return
     */
    public <R,ID> R handleBreakWithLogicalExpire(
            String keyPrefix,String lockKeyPrefix,ID id,Class<R> type,Function<ID,R> queryDB,Long time, TimeUnit unit){

        String key=keyPrefix + id;
        // 1. 从redis查询商铺缓存
        String json = stringRedisTemplate.opsForValue().get(key);
        //2.判断缓存redis中是否存在----------------------------------
        if(StrUtil.isBlank(json)){
            // 3.未命中，直接返回
            return null;
        }
        //4.redis中命中，需要先把json反序列化为对象
        //TODO:这里不懂
        RedisData redisData = JSONUtil.toBean(json, RedisData.class);
        JSONObject data = (JSONObject) redisData.getData();
        R r = JSONUtil.toBean(data, type);
        LocalDateTime expireTime = redisData.getExpireTime();//从data中取出过期时间
        //4.1判断是否过期
        if(expireTime.isAfter(LocalDateTime.now())){
            //// 4.2. 未过期，直接返回店铺信息
            return r;
        }

        //// 4.3.已过期，需要缓存重建
        //5.缓存重建
        //5.1.获取互斥锁
        //TODO:下面这个lockkey要记得改
        String lockKey=lockKeyPrefix+id;
        boolean isLock = tryLock(lockKey);
        //5.2.判断是否获取锁成功
        if(isLock){
            //5.3获取互斥锁成功则开启新线程，实现缓存重建
            CACHE_REBUILD_THREADPOOLS.submit(()->{
                try {
                    //重建缓存
                    //查询数据库
                    R r1=queryDB.apply(id);
                    //写入redis
                    this.setWithLogicalExpire(key,r1,time,unit);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    //释放锁
                    unlock(lockKey);
                }
            });
        }
        //5.4.获取互斥锁失败则返回 旧 的信息
        return r;
    }

    //-----------------------------------工具类------------------------
    //尝试获取锁
    private boolean tryLock(String key){
        //setIfAbsent等效于setnx:加锁
        Boolean flag = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", 10, TimeUnit.SECONDS);//时间是10秒
        return BooleanUtil.isTrue(flag);
    }

    //释放锁
    private void unlock(String key){
        stringRedisTemplate.delete(key);
    }

}
