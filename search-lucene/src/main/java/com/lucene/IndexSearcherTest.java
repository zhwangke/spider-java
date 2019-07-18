package com.lucene;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.wltea.analyzer.lucene.IKAnalyzer;

import java.io.File;

//索引查询的入门代码
public class IndexSearcherTest {


    public static void main(String[] args) throws  Exception {
        //1. 创建lucene用于查询索引核心对象
        IndexReader r  = DirectoryReader.open(FSDirectory.open(new File("e:\\index")));
        IndexSearcher indexSearcher = new IndexSearcher(r);

        //2. 添加查询的条件
        //2.1 通过查询解析器的方式来获取query对象
        QueryParser queryParser = new QueryParser("content",new IKAnalyzer());
        // 参数: 表示的是用户输入的内容
        Query query = queryParser.parse("蓝瘦香菇吊炸天");
        //3. 执行查询
        // 参数1: 查询的条件
        //参数2:  查询前几个
        // 返回值: 结果集
        //      主要包含二部分的内容:  1)  查询的结果集的得分数组   2) 查询的总条数
        TopDocs topDocs = indexSearcher.search(query, Integer.MAX_VALUE);

        //4. 获取数据
        ScoreDoc[] scoreDocs = topDocs.scoreDocs; // 得分文档的数组
        int totalHits = topDocs.totalHits; //查询的总条数
        // ScoreDoc :得分文档对象, 包含二部分内容:  1)  文档的id  2) 文档的得分
        for (ScoreDoc scoreDoc : scoreDocs) {
            float score = scoreDoc.score; //得分
            int docId = scoreDoc.doc; //文档的id
            Document document = indexSearcher.doc(docId);
            String id = document.get("id");
            String title = document.get("title");
            String content = document.get("content");

            System.out.println("文档的得分为:" + score +" 文档的id:"+id+" 文档的标题:"+title+" 文档的内容:"+ content);
        }
    }
}