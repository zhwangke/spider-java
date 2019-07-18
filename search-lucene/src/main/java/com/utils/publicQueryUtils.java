package com.utils;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.io.IOException;

/**
 * @Author: WK
 * @Data: 2019/7/17 20:30
 * @Description: com.utils
 */
public class publicQueryUtils {
    /**
     * 查询索引的工具类
     * @param
     */
    public static void publicQuery(Query query) throws IOException {
        //1创建lucene查询索引的核心对象
        //存放索引的磁盘路径
        Directory d = FSDirectory.open(new File("e:\\index"));
        IndexReader r = DirectoryReader.open(d);

        IndexSearcher indexSearcher = new IndexSearcher(r);
        //2添加查询的条件
        //- 参数1:  查询条件
        //- 参数2: 返回的最大条数
        TopDocs topDocs = indexSearcher.search(query, Integer.MAX_VALUE);

        //4获取数据
        ScoreDoc[] scoreDocs = topDocs.scoreDocs;//得分文档的数组
        int totalHits = topDocs.totalHits;//查询的总条数
        // ScoreDoc : 包含二部分内容:  1)  文档的id  2) 文档的得分

        for (ScoreDoc scoredoc:scoreDocs) {
            float score = scoredoc.score;
            int docId = scoredoc.doc;

            Document document = indexSearcher.doc(docId);
            String id = document.get("id");
            String title = document.get("title");
            String content = document.get("content");

            System.out.println("文档的得分为:" + score +" 文档的id:"+id+" 文档的标题:"+title+" 文档的内容:"+ content);
        }
    }
}
