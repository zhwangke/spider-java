package com.pojo;

import org.apache.solr.client.solrj.beans.Field;

import java.io.Serializable;

/**
 * @Author: WK
 * @Data: 2019/7/19 20:16
 * @Description: com.pojo
 */
public class News implements Serializable {
    @Field
    private String id;
    @Field
    private String title;
    @Field
    private String content;
    @Field
    private String docurl;

    public News() {
    }

    public News(String id, String title, String content, String docurl) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.docurl = docurl;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getDocurl() {
        return docurl;
    }

    public void setDocurl(String docurl) {
        this.docurl = docurl;
    }

    @Override
    public String toString() {
        return "News{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", docurl='" + docurl + '\'' +
                '}';
    }
}
