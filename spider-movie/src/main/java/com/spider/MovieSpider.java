package com.spider;


import com.dao.MaoyanTopDao;
import com.domain.MaoyanTop;
import com.utils.HttpClientUtils;
import com.utils.JedisUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import redis.clients.jedis.Jedis;

import java.io.IOException;

/**
 * @Author: WK
 * @Data: 2019/7/15 10:53
 * @Description: com.spider
 */
public class MovieSpider {
    public static MaoyanTopDao maoyanTopDao = new MaoyanTopDao();
    public static void main(String[] args) throws IOException {
        /**
         * 1.确定首页url
         */
        int page = 0;
        //获取全部链接
        for (page=0;page<100;page+=10){
            String indexUrl = "https://maoyan.com/board/4?offset=" + page;
            String html = HttpClientUtils.doGet(indexUrl);
            Document document = Jsoup.parse(html);
            Elements links = document.select("div.movie-item-info > p.name > a");
            for (Element link : links) {
                String href = "https://maoyan.com" + link.attr("href");
                System.out.println(href);
                //###############去重代码#################
                Jedis jedis = JedisUtils.getJedis();
                Boolean flag = jedis.sismember("bigdata:spider:movie", href);
                jedis.close();
                if (flag){
                    System.out.println("数据已存在...");
                    //表示已经爬取此链接 跳过此次爬取
                    continue;
                }
                //###############去重代码#################
                //解析数据 并封装javabean
                MaoyanTop maoyanTop = parseJson(href);
                //保存数据
                maoyanTopDao.saveMovie(maoyanTop);
                //###############去重代码#################
                jedis = JedisUtils.getJedis();
                jedis.sadd("bigdata:spider:movie",maoyanTop.getHref());
                jedis.close();
                //###############去重代码#################
                
            }
        }
    }
    private static MaoyanTop parseJson(String href) throws IOException {
       //发送请求
        String html = HttpClientUtils.doGet(href);
        Document document = Jsoup.parse(html);

        /**
         * 解析html 获取数据
         */
        Elements title = document.select("div.movie-brief-container > h3");
        Elements type = document.select("div.movie-brief-container > ul > li:nth-child(1)");
        Elements location = document.select("div.movie-brief-container > ul > li:nth-child(2)");
        Elements version = document.select("div.movie-brief-container > ul > li:nth-child(3)");
        Elements info = document.select("div.mod-content>span.dra");
        MaoyanTop maoyanTop = new MaoyanTop();
        //电影名字
        String titles = title.text();
        maoyanTop.setTitles(titles);
        //电影类型
        String types = type.text();
        maoyanTop.setTypes(types);
        String s = location.text();
        String[] split = s.split(" / ");
        //上映地点
        String local = split[0];
        maoyanTop.setLocal(local);
        //上映时间
        String time = version.text().substring(0,4);
        maoyanTop.setTime(time);
        //电影简介
        String infos = info.text();
        maoyanTop.setInfos(infos);
        //电影时长
        String hour = split[1];
        maoyanTop.setHour(hour);
        maoyanTop.setHref(href);
        return maoyanTop;
    }
}
