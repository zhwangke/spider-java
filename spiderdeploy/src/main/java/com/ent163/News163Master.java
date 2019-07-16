package com.ent163;

/**
 * @Author: WK
 * @Data: 2019/7/16 19:27
 * @Description: com.ent163
 */
import com.google.gson.Gson;
import com.utils.HttpClientUtils;
import com.utils.JedisUtils;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 1)  确定首页url 2) 发送请求, 获取数据 3) 解析数据 4) 去重判断  5) 将docurl保存到redis中 6) 获取下一页
 */
public class News163Master {

    public static void main(String[] args) throws Exception {

        //1. 确定首页url:
        List<String> urlList = new ArrayList<String>();
        urlList.add("https://ent.163.com/special/000380VU/newsdata_index.js?callback=data_callback");
        urlList.add("https://ent.163.com/special/000380VU/newsdata_star.js?callback=data_callback");
        urlList.add("https://ent.163.com/special/000380VU/newsdata_movie.js?callback=data_callback");
        urlList.add("https://ent.163.com/special/000380VU/newsdata_tv.js?callback=data_callback");
        urlList.add("https://ent.163.com/special/000380VU/newsdata_show.js?callback=data_callback");
        urlList.add("https://ent.163.com/special/000380VU/newsdata_music.js?callback=data_callback");

        //5. 分页获取数据
        while(!urlList.isEmpty()) {
            String indexUrl = urlList.remove(0);
            System.out.println("获取了下一个栏目的数据#######################################" );
            page(indexUrl);
        }
    }
    // 执行分页的方法

    public static void  page(String indexUrl) throws  Exception{
        String page = "02";
        while(true) {
            //1. 发送请求获取数据
            // 此处获取的json的数据, 并不是一个非标准的json

            String jsonStr = HttpClientUtils.doGet(indexUrl);
            if(jsonStr==null){
                System.out.println("数据获取完成");
                break;
            }
            // 转换为标准json方法
            jsonStr = splitJson(jsonStr);

            //2. 解析数据, 3 保存数据
            parseJson(jsonStr);

            //4. 获取下一页的url
            if(indexUrl.contains("newsdata_index")){
                indexUrl = "https://ent.163.com/special/000380VU/newsdata_index_" + page + ".js?callback=data_callback";
            }
            if(indexUrl.contains("newsdata_star")){
                indexUrl = "https://ent.163.com/special/000380VU/newsdata_star_" + page + ".js?callback=data_callback";
            }
            if(indexUrl.contains("newsdata_movie")){
                indexUrl = "https://ent.163.com/special/000380VU/newsdata_movie_" + page + ".js?callback=data_callback";
            }
            if(indexUrl.contains("newsdata_tv")){
                indexUrl = "https://ent.163.com/special/000380VU/newsdata_tv_" + page + ".js?callback=data_callback";
            }
            if(indexUrl.contains("newsdata_show")){
                indexUrl = "https://ent.163.com/special/000380VU/newsdata_show_" + page + ".js?callback=data_callback";
            }
            if(indexUrl.contains("newsdata_music")){
                indexUrl = "https://ent.163.com/special/000380VU/newsdata_music_" + page + ".js?callback=data_callback";
            }


            System.out.println(indexUrl);

            //5. page ++
            int pageNum = Integer.parseInt(page);
            pageNum++;

            if(pageNum <10){
                page = "0"+pageNum;
            }else{
                page = pageNum+"";
            }
        }

    }

    // 解析json的方法
    private static void parseJson(String jsonStr)  throws  Exception{
        //3.1 将json字符串转换成 指定的对象
        Gson gson = new Gson();

        List<Map<String, Object>> newsList = gson.fromJson(jsonStr, List.class);
        // 3.2 遍历整个新闻的结合, 获取每一个新闻的对象
        for (Map<String, Object> newsObj : newsList) {
            // 新闻 :  标题, 时间,来源 , 内容 , 新闻编辑  ,  新闻的url
            //3.2.1 获取新闻的url , 需要根据url, 获取详情页中新闻数据
            String docUrl = (String) newsObj.get("docurl");
            // 过滤掉一些不是新闻数据的url
            if(docUrl.contains("photoview")){
                continue;
            }
            if(docUrl.contains("v.163.com")){
                continue;
            }
            if(docUrl.contains("c.m.163.com")){
                continue;
            }
            if(docUrl.contains("dy.163.com")){
                continue;
            }
            // ###################去重处理代码######################
            Jedis jedis = JedisUtils.getJedis();
            Boolean flag = jedis.sismember("bigData:spider:docurl", docUrl);
            jedis.close();//一定一定一定不要忘记关闭, 否则用着用着没了, 导致程序卡死不动
            if(flag){
                // 代表存在, 表示已经爬取过了
                continue;
            }
            // ###################去重处理代码######################
            // 将docurl存储到redis的list集合中
            jedis = JedisUtils.getJedis();
            jedis.lpush("bigData:spider:163itemUrl:docurl",docUrl);
            jedis.close();

        }
    }
    // 将非标准的json转换为标准的json字符串
    private static String splitJson(String jsonStr) {
        int firstIndex = jsonStr.indexOf("(");
        int lastIndex = jsonStr.lastIndexOf(")");

        return jsonStr.substring(firstIndex + 1, lastIndex);

    }
}

