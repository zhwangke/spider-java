package com.solrj;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CloudSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.junit.Test;

import java.io.IOException;

/**
 * @Author: WK
 * @Data: 2019/7/20 19:24
 * @Description: com.solrj
 */
//solrj连接集群, 基本的索引库CURD操作
public class solrjCloud {
    //添加索引数据
    @Test
    public void indexWriterTest01() throws IOException, SolrServerException {
        //1.创建一个solrj连接solrCloud的服务对象
        // 如果想要使用 node01 : 2181 这种方式, 必须保证,
        // node01 是在本地Windows环境下的hosts文件中配置上
        //C:\Windows\System32\drivers\etc
        // 验证是否配置:  打开cmd  执行: ping node01
        String zkHost = "node01:2181,node02:2181,node03:2181";
        CloudSolrServer solrServer = new CloudSolrServer(zkHost);

        //2设置相关参数  一个必选两个可选
        //默认连接的索引库
        solrServer.setDefaultCollection("collection2");
        //从zk中获取链接的超时时间
        solrServer.setZkConnectTimeout(5000);
        //链接zk的超时时间
        solrServer.setZkClientTimeout(5000);

        //3从zk中获取链接
        solrServer.connect();

        //4执行添加数据的操作
        SolrInputDocument doc = new SolrInputDocument();
        doc.addField("id",1);
        doc.addField("title","世界上最傻的焊接工");
        doc.addField("content","其实还有比你更傻的, 把自己缝在被罩了");
        solrServer.add(doc);

        //5提交数据
        solrServer.commit();

    }
    @Test
    public void indexDelete() throws IOException, SolrServerException {
        //1.1创建一个solrj链接solrCloud服务的对象
        String zkHost = "node01:2181,node02:2181,node03:2181";
        CloudSolrServer solrServer = new CloudSolrServer(zkHost);
        //1.2设置相关参数
        solrServer.setDefaultCollection("collection2");

        //设置链接超时的时间
        solrServer.setZkConnectTimeout(5000);
        //设置链接客户端超时的时间
        solrServer.setZkClientTimeout(5000);

        //1.3获取链接
        solrServer.connect();

        //2添加删除的条件
        solrServer.deleteById("1");
        //solrServer.deleteByQuery("id",1);

        //3提交数据
        solrServer.commit();
    }
    //查询索引
    @Test
    public void indexSearcherTest() throws SolrServerException {
        //1. 创建一个solrj连接solrCloud的服务对象
        String zkHost = "node01:2181,node02:2181,node03:2181";
        CloudSolrServer solrServer = new CloudSolrServer(zkHost);

        //1.1 设置相关的参数: 1个必须 两个可选
        solrServer.setDefaultCollection("collection2");

        //1.2 获取连接
        solrServer.connect();

        //2封装查询的条件
        SolrQuery solrQuery = new SolrQuery("*:*");

        //3执行查询
        QueryResponse response = solrServer.query(solrQuery);

        //4获取数据
        SolrDocumentList results = response.getResults();
        for (SolrDocument document: results) {
            String id = (String) document.get("id");
            String title = (String) document.get("title");
            String content = (String) document.get("content");
            System.out.println(id+"---"+title+"---"+content);
        }


    }

}
