package com.ent163;

/**
 * @Author: WK
 * @Data: 2019/7/16 19:30
 * @Description: com.ent163
 */
import com.dao.NewsDao;
import com.domain.News;
import com.google.gson.Gson;
import com.utils.JedisUtils;
import redis.clients.jedis.Jedis;

import java.util.List;

// 需求 : 公共的保存数据库的程序:
// 步骤:  1) 从redis中获取newsJson数据   2) 将newsJson转换成news对象  3) 去重判断  4) 保存数据
//       5) 将docurl存储到redis的去重的set集合中   6) 循环
public class PublicDaoNode {
    private static NewsDao newsDao = new NewsDao();

    public static void main(String[] args) {

        while(true) {
            //1) 从redis中获取newsJson数据
            Jedis jedis = JedisUtils.getJedis();
            List<String> list = jedis.brpop(20, "bigData:spider:newsJson");
            jedis.close();
            if (list == null || list.size() == 0) {
                break;
            }
            String newsJson = list.get(1);
            System.out.println(newsJson);
            //2. 将newsJson转换成news对象
            Gson gson = new Gson();
            News news = gson.fromJson(newsJson, News.class);

            //3) 去重判断
            jedis = JedisUtils.getJedis();
            Boolean flag = jedis.sismember("bigData:spider:docurl", news.getDocurl());
            jedis.close();

            if (flag) {
                continue;
            }
            //4) 保存数据

            newsDao.saveNews(news);

            // 5)  将docurl存储到redis的去重的set集合中
            jedis = JedisUtils.getJedis();
            jedis.sadd("bigData:spider:docurl", news.getDocurl());
            jedis.close();
        }

    }

}