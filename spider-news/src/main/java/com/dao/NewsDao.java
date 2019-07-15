package com.dao;

import com.domain.News;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.springframework.jdbc.core.JdbcTemplate;

import java.beans.PropertyVetoException;

/**
 * @Author: WK
 * @Data: 2019/7/14 20:47
 * @Description: com.dao
 */
public class NewsDao extends JdbcTemplate {

    public NewsDao() {
        ComboPooledDataSource dataSource = new ComboPooledDataSource();

        try {
            dataSource.setDriverClass("com.mysql.jdbc.Driver");
            dataSource.setJdbcUrl("jdbc:mysql:///news"); // 需要更改为自己的数据库名称
            dataSource.setUser("root");
            dataSource.setPassword("123"); // 需要更改为自己的mysql的密码
        } catch (PropertyVetoException e) {
            e.printStackTrace();
        }

        super.setDataSource(dataSource);
    }


    // 保存数据的方法
    public void saveNews(News news) {
        String[] params = {news.getId(), news.getTitle(), news.getTime(), news.getSource(), news.getContent(), news.getEditor(), news.getDocurl()};
        update("INSERT  INTO  news VALUES (?,?,?,?,?,?,?)", params);
    }
}
