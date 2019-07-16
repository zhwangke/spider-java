package com.ent163;

/**
 * @Author: WK
 * @Data: 2019/7/16 19:30
 * @Description: com.ent163
 */
import com.domain.News;
import com.google.gson.Gson;
import com.utils.HttpClientUtils;
import com.utils.IdWorker;
import com.utils.JedisUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import redis.clients.jedis.Jedis;

import java.util.List;

// 需求 :  解析新闻详情页的数据
// 步骤:  1) 从redis中获取docurl  2) 根据url解析商品的详情页 3) 封装成news对象  4) 将news对象转换为json数据
//          5) 将newsJson存储到redis的list中  6) 循环
public class News163Slave {
    private static IdWorker idWorker  = new IdWorker(1,1);
    public static void main(String[] args) throws Exception {
        while (true) {
            //1.从redis中获取docurl
            Jedis jedis = JedisUtils.getJedis();

            //String docurl = jedis.rpop("bigData:spider:163itemUrl:docurl");  // 取不到的时候出来
            //  第一个参数: 阻塞的时间   如果list中没有数据了, 就会进行阻塞, 最多阻塞20s. 如果在23s之内有数据进来, 马上解除阻塞
            // 返回值:  list    在这个list中只会有两个元素, 第一个元素为key值  第二个元素为弹出的元素
            List<String> list = jedis.brpop(20, "bigData:spider:163itemUrl:docurl");
            jedis.close();
            if(list == null || list.size()==0 ){
                break;
            }
            String docurl = list.get(1);
            //2. 根据url解析商品的详情页 封装成news对象
            News news = parseNewsItem(docurl);

            //3. 将news对象转换为json数据
            Gson gson = new Gson();
            String newsJson = gson.toJson(news);

            //4. 将newsJson存储到redis中
            jedis = JedisUtils.getJedis();
            jedis.lpush("bigData:spider:newsJson", newsJson);
            jedis.close();
        }
    }

    // 根据url 解析新闻详情页:
    private static News parseNewsItem(String docUrl) throws Exception {
        System.out.println(docUrl);
        //  3.3.1 发送请求, 获取新闻详情页数据
        String html = HttpClientUtils.doGet(docUrl);

        //3.3.2 解析新闻详情页:
        Document document = Jsoup.parse(html);

        //3.3.2.1 :  解析新闻的标题:
        News news = new News();
        Elements h1El = document.select("#epContentLeft h1");
        String title = h1El.text();
        news.setTitle(title);

        //3.3.2.2 :  解析新闻的时间:
        Elements timeAndSourceEl = document.select(".post_time_source");

        String timeAndSource = timeAndSourceEl.text();

        String[] split = timeAndSource.split("　来源: ");// 请各位一定一定一定要复制, 否则会切割失败
        news.setTime(split[0]);
        //3.3.2.3 :  解析新闻的来源:
        news.setSource(split[1]);
        //3.3.2.4 :  解析新闻的正文:
        Elements ps = document.select("#endText p");
        String content = ps.text();
        news.setContent(content);
        //3.3.2.5 :  解析新闻的编辑:
        Elements spanEl = document.select(".ep-editor");
        // 责任编辑：陈少杰_b6952
        String editor = spanEl.text();
        // 一定要接收返回值, 否则白写了
        editor = editor.substring(editor.indexOf("：") + 1, editor.lastIndexOf("_"));
        news.setEditor(editor);
        //3.3.2.6 :  解析新闻的url:
        news.setDocurl(docUrl);
        //3.3.2.7: id
        long id = idWorker.nextId();
        news.setId(id + "");

        return news;
    }
}
