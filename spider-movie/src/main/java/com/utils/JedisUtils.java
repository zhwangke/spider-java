package com.utils;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class JedisUtils {
    private static JedisPool jedisPool;
   // 静态代码块 :  随着类的加载而加载, 一般只会加载一次
    static{
       JedisPoolConfig config = new JedisPoolConfig();
       config.setMaxTotal(100); // 最大的连接数
       config.setMaxIdle(50); // 最大闲时数量
       config.setMinIdle(25); // 最小闲时数量
       // 注意: 如果写成这样, JedisPool jedisPool , 当获取连接对象的时候, 会报空指针错误
       jedisPool = new JedisPool(config,"192.168.72.142",6379);
    }



    //获取连接的方法
    public static Jedis getJedis(){

        return jedisPool.getResource();
    }
}
