package com.tencent;

/**
 * @Author: WK
 * @Data: 2019/7/16 19:34
 * @Description: com.tencent
 */

import com.domain.News;
import com.google.gson.Gson;
import com.utils.HttpClientUtils;
import com.utils.IdWorker;
import com.utils.JedisUtils;
import redis.clients.jedis.Jedis;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
    // 需求 :  解析数据, 封装成news对象, 将news对象保存到redis中
//  1) 确定首页url  2) 发送请求, 获取数据  3) 解析数据 4)  去重判断  5) 封装news对象  6) 将news对象转换为newsJson
//  7) 将newsJson保存到Redis中   8) 分页获取
    public class NewsTencentMaster {
        private static IdWorker idWorker = new IdWorker(0,2);

        public static void main(String[] args) throws Exception {
            //1. 确定首页url
            String topNewsUrl = "https://pacaio.match.qq.com/irs/rcd?cid=137&token=d0f13d594edfc180f5bf6b845456f3ea&ext=ent&num=60";
            String noTopNewsUrl = "https://pacaio.match.qq.com/irs/rcd?cid=146&token=49cbb2154853ef1a74ff4e53723372ce&ext=ent&page=0";

            //2. 执行分页:
            page(topNewsUrl, noTopNewsUrl);
        }
        // 执行分页的方法
        public static void page(String topNewsUrl, String noTopNewsUrl) throws Exception {
            //1. 热点新闻数据的获取:  只有一页数据
            //1.1 发送请求, 获取数据
            String topNewsJsonStr = HttpClientUtils.doGet(topNewsUrl);
            //1.2 解析数据
            List<News> topNewsList = parseJson(topNewsJsonStr);
            //1.3 保存数据
            saveNews(topNewsList);

            //2. 处理非热点数据
            int page = 1;
            while (true) {

                //2.1 发送请求, 获取数据
                String noTopNewsJsonStr = HttpClientUtils.doGet(noTopNewsUrl);
                //2.2 解析数据
                List<News> noTopNewsList = parseJson(noTopNewsJsonStr);

                if (noTopNewsList == null) {
                    break;
                }
                //2.3 保存数据
                saveNews(noTopNewsList);
                //2.4 获取下一页url
                noTopNewsUrl = "https://pacaio.match.qq.com/irs/rcd?cid=146&token=49cbb2154853ef1a74ff4e53723372ce&ext=ent&page=" + page;

                //2.5 自增 +1
                page++;

                System.out.println(page);
            }
        }
        // 保存数据的操作 : 腾讯返回数据的时候, 就会有重复的数据
        public static void saveNews(List<News> newsList) {
            Jedis jedis = JedisUtils.getJedis();
            Gson gson = new Gson();
            for (News news : newsList) {
                // 需要将news对象转换为newsJson
                String newsJson = gson.toJson(news);

                // 将newsJson存储到redis的list集合中
                jedis.lpush("bigData:spider:newsJson",newsJson);
            }
            jedis.close();
        }
        //  解析新闻数据
        private static List<News> parseJson(String newsJsonStr) {
            //3.1 将字符串json数据转换为指定的类型:   map
            Gson gson = new Gson();
            Map<String, Object> map = gson.fromJson(newsJsonStr, Map.class);
            //获取一下, 本次获取了多少条数据
            Double datanum = (Double) map.get("datanum");
            if (datanum.intValue() == 0) {
                return null;
            }
            //3.2  获取data中数据 : 列表页中数据
            List<Map<String, Object>> newsList = (List<Map<String, Object>>) map.get("data");
            //3.3 遍历这个列表, 获取每一个新闻的数据
            List<News> tencentNewList = new ArrayList<News>();
            for (Map<String, Object> newsMap : newsList) {
                String docurl = (String) newsMap.get("vurl");
                if (docurl.contains("video")) {
                    continue;
                }
                //######################去重处理############################33
                Jedis jedis = JedisUtils.getJedis();
                Boolean flag = jedis.sismember("bigData:spider:docurl", docurl);
                jedis.close();
                if (flag) {
                    // 如果为true, 表示已经存在, 已经爬取过了
                    continue;
                }

                //######################去重处理############################33

                //3.3.1 封装news对象
                News news = new News();

                news.setTitle((String) newsMap.get("title"));
                news.setTime((String) newsMap.get("update_time"));
                news.setSource((String) newsMap.get("source"));
                news.setContent((String) newsMap.get("intro"));
                news.setEditor((String) newsMap.get("source"));
                news.setDocurl(docurl);

                news.setId(idWorker.nextId() + "");

                tencentNewList.add(news);

                System.out.println(docurl);
            }

            return tencentNewList;
        }
    }