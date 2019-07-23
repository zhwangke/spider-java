package com.utils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import redis.clients.jedis.Jedis;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class HttpClientUtils {
    private static PoolingHttpClientConnectionManager connectionManager;

    static {
        //定义一个连接池的工具类对象
        connectionManager = new PoolingHttpClientConnectionManager();
        //定义连接池属性
        //定义连接池最大的连接数
        connectionManager.setMaxTotal(200);
        //定义主机的最大的并发数
        connectionManager.setDefaultMaxPerRoute(20);
        initIp();
    }

    //获取closeHttpClient
    private static CloseableHttpClient getCloseableHttpClient() {

        CloseableHttpClient httpClient = HttpClients.custom().setConnectionManager(connectionManager).build();

        return httpClient;
    }
    private static CloseableHttpClient getProxyHttpClient() {

        // 从redis中获取代理IP
        Jedis conn = JedisUtils.getJedis();
        // 从右边弹出一个元素之后，从新放回左边
        List<String> ipkv = conn.brpop(0, "spider:ip");
        // CloseableHttpClient httpClient = getHttpClient();
        String[] vals = ipkv.get(0).split(":");
        System.out.println(vals);
        HttpHost proxy = new HttpHost(vals[0], Integer.parseInt(vals[1]));
        DefaultProxyRoutePlanner routePlanner = new DefaultProxyRoutePlanner(proxy);
        return HttpClients.custom().setConnectionManager(connectionManager).setRoutePlanner(routePlanner).build();
    }
    private static void initIp() {
        try {
            Jedis conn = JedisUtils.getJedis();
            BufferedReader bufferedReader = null;
            bufferedReader = new BufferedReader(
                    new FileReader(new File("C:\\Users\\maoxiangyi\\Desktop\\Proxies2018-06-06.txt")));
            String line = null;
            while ((line=bufferedReader.readLine())!=null) {
                conn.lpush("spider:ip", line);
            }
            bufferedReader.close();
            conn.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
        }
    }

    //执行请求返回HTML页面
    private static String execute(HttpRequestBase httpRequestBase) throws IOException {

        httpRequestBase.setHeader("User-Agent","Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/35.0.1916.153 Safari/537.36");
        /**
         * setConnectionRequestTimeout:设置获取请求的最长时间
         *
         * setConnectTimeout: 设置创建连接的最长时间
         *
         * setSocketTimeout: 设置传输超时的最长时间
         */

        RequestConfig config = RequestConfig.custom().setConnectionRequestTimeout(5000).setConnectTimeout(5000)
                .setSocketTimeout(10 * 1000).build();

        httpRequestBase.setConfig(config);


        CloseableHttpClient httpClient = getCloseableHttpClient();

        CloseableHttpResponse response = httpClient.execute(httpRequestBase);
        String html;
        if(response.getStatusLine().getStatusCode()==200){
            html = EntityUtils.toString(response.getEntity(), "GBK");
        }else{
            html = null;
        }



        return html;
    }

    //get请求执行
    public static String doGet(String url) throws IOException {
        HttpGet httpGet = new HttpGet(url);

        String html = execute(httpGet);

        return html;

    }

    //post请求执行
    public static String doPost(String url, Map<String, String> param) throws Exception {
        HttpPost httpPost = new HttpPost(url);

        List<BasicNameValuePair> list = new ArrayList<BasicNameValuePair>();

        for (String key : param.keySet()) {

            list.add(new BasicNameValuePair(key, param.get(key)));
        }
        HttpEntity entity = new UrlEncodedFormEntity(list);
        httpPost.setEntity(entity);

        return execute(httpPost);
    }
}
