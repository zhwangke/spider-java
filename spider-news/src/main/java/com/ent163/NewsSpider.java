package com.ent163;

import com.dao.NewsDao;
import com.domain.News;
import com.google.gson.Gson;
import com.utils.HttpClientUtils;
import com.utils.IdWorker;
import com.utils.JedisUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import redis.clients.jedis.Jedis;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @Author: WK
 * @Data: 2019/7/14 19:16
 * @Description: com.ent163
 */
public class NewsSpider {
    private static IdWorker idWorker  = new IdWorker(0,1);
    private static  NewsDao newsDao = new NewsDao();
    public static void main(String[] args) throws IOException {
        //确定首页url
        ArrayList<String> list = new ArrayList<String>();
        list.add("https://ent.163.com/special/000380VU/newsdata_index.js?");
        list.add("https://ent.163.com/special/000380VU/newsdata_star.js?");
        list.add("https://ent.163.com/special/000380VU/newsdata_movie.js?");
        list.add("https://ent.163.com/special/000380VU/newsdata_tv.js?");
        list.add("https://ent.163.com/special/000380VU/newsdata_show.js?");
        list.add("https://ent.163.com/special/000380VU/newsdata_music.js?");
        //5. 分页获取数据
        while(!list.isEmpty()) {
            String indexUrl = list.remove(0);
            System.out.println("获取了下一个栏目的数据#######################################");
            page(indexUrl);
        }
    }

    /**
     * 执行分页的方法
     */
    private static void page(String indexUrl) throws IOException {
        String page = "02";
        while (true){
            //发送请求获取数据
            String html = HttpClientUtils.doGet(indexUrl);
            if (html==null){
                System.out.println("数据获取完成...");
                break;
            }
            //转换为标准的json格式
            html = splitJson(html);
            //解析json数据获取docurl并保存数据
            parseJson(html);

            //获取下一页的url
//            indexUrl = "https://ent.163.com/special/000380VU/newsdata_index_"+page+".js?";

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
            //把字符串页码转换为整数
            int pageNum = Integer.parseInt(page);
            pageNum++;
            if (pageNum<10){
                page = "0"+pageNum;
            }else {
                page = pageNum+"";
            }
        }

    }
    /**
     * 解析json数据获取docurl
     * @param splitJson
     */
    private static void parseJson(String splitJson) throws IOException {
        //将json字符串转换成 指定的对象
        Gson gson = new Gson();
        List<Map<String,Object>> list = gson.fromJson(splitJson,List.class);

        //遍历集合
        int count = 0;
        for (Map<String,Object> map:list) {
            String docurl = (String) map.get("docurl");
            // 过滤掉一些不是新闻数据的url
            if(docurl.contains("photoview")){
                continue;
            }
            if (docurl.contains("ent.163.com")){
//                System.out.println(docurl);
                //##########去重代码##########
                Jedis jedis = JedisUtils.getJedis();
                Boolean flag = jedis.sismember("bigdata:spider:163spider:docurl",docurl);
                /**
                 * 一定要关闭
                 */
                jedis.close();
                if (flag){
                    System.out.println("内容已存在...");
                    //表示redis有了此url 已经爬取过 所以跳出此次循环
                    continue;
                }
                //##########去重代码##########
                //获取详情页的数据
                News news = parseNewJson(docurl);
                //保存数据到mysql
                newsDao.saveNews(news);
                System.out.println(news.getTitle()+"的内容保存完毕...");
                count++;
                //##########去重代码##########
                jedis = JedisUtils.getJedis();
                jedis.sadd("bigdata:spider:163spider:docurl",news.getDocurl());
                jedis.close();
                //##########去重代码##########
            }

        }
        System.out.println(count);
    }

    /***
     * 获取详情页的信息
     * @param docUrl
     */
    private static News parseNewJson(String docUrl) throws IOException {
        //发送请求获取数据
        String html = HttpClientUtils.doGet(docUrl);
        //解析html文件
        Document document = Jsoup.parse(html);

        News news = new News();
        //文章标题
        Elements title = document.select("#epContentLeft >h1");
        news.setTitle(title.text());
        //文章时间
        Elements time = document.select(".post_time_source");
        String[] split = time.text().split("　来源: ");
        String s = split[0];
        news.setTime(s);
        //文章来源
        Elements source = document.select("#ne_article_source");
        news.setSource(source.text());
        //文章内容
        Elements content = document.select("#endText p");
        news.setContent(content.text());
        //文章编辑
        Elements editor = document.select(".ep-editor");
        String substring = editor.text().substring(editor.text().indexOf("：")+1,editor.text().lastIndexOf("_"));
        news.setEditor(substring);
        //文章url
        news.setDocurl(docUrl);
        //文章id
        news.setId(new IdWorker().nextId()+"");
        return news;
    }

    /**
     * 转换为标准的json格式
     * @param html
     * @return
     */
    private static String splitJson(String html) {
        String json = html.substring(html.indexOf("(")+1, html.indexOf(")"));
        return json;
    }
}
