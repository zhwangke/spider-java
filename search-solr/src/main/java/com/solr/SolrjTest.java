package com.solr;

import com.pojo.News;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.common.SolrInputDocument;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author: WK
 * @Data: 2019/7/19 19:58
 * @Description: com.solr
 */
public class SolrjTest {
    /**
     * 写入索引的原生操作
     */
    @Test
    public void indexWriterTest01() throws Exception{

        //1. 创建solrj连接solr的远程服务对象 : #表示的页面的路径 不带#号才是访问的接口路径
        SolrServer solrServer = new HttpSolrServer("http://localhost:8080/solr/collection1");
        //2. 添加文档数据
        SolrInputDocument doc = new SolrInputDocument();
        doc.addField("id",1);
        doc.addField("title","连休三天！端午节放假通知来了");
        doc.addField("content","请广大市民提前安排好工作生活，节日期间注意安全，度过一个平安、祥和的节日假期。");

        solrServer.add(doc);

        //3. 提交数据
        solrServer.commit();
    }
    /***
     * 一次性写入多条数据
     *
     */
    @Test
    public void indexWriterTest02() throws IOException, SolrServerException {
        SolrServer solrServer = new HttpSolrServer("http://localhost:8080/solr/collection2");
        //2添加文档数据
        List<SolrInputDocument> docs = new ArrayList<SolrInputDocument>();
        for (int i = 0; i < 7; i++) {
            SolrInputDocument doc = new SolrInputDocument();
            doc.addField("id",i);
            doc.addField("title","solr的简介"+i);
            doc.addField("content","solr是apache官方提供企业级的搜索应用服务器, 连接solr需要发送http请求, solr的底层是lucene "+i);
            docs.add(doc);
        }
        solrServer.add(docs);
        //3提交数据
        solrServer.commit();
    }

    /**
     * 直接写入一个javaBean对象
     * @throws Exception
     */
    @Test
    public void indexWriterTest03() throws  Exception {
        SolrServer solrServer = new HttpSolrServer("http://localhost:8080/solr/collection1");
        //添加文档数据
        News news = new News("8","国务院定了！11月底前在全国全面实施“携号转网”","国务院总理李克强5月14日主持召开国务院常务会议，部署进一步推动网络提速降费，发挥扩内需稳就业惠民生多重效应","http://baijiahao.baidu.com/s?id=1633615346318630206");
        solrServer.addBean(news);
        //提交数据
        solrServer.commit();

    }
    //
    // 删除索引
    @Test
    public void delIndexTest04() throws  Exception{
        //1. 创建solrj连接solr的而服务对象
        SolrServer solrServer = new HttpSolrServer("http://localhost:8080/solr/collection2");

        //2. 添加删除的条件
        //solrServer.deleteById("change.me");  根据id删除
        solrServer.deleteByQuery("*:*"); //删除所有
//        solrServer.deleteByQuery("id:1"); //删除所有
        //3. 执行删除
        solrServer.commit();
    }

}
