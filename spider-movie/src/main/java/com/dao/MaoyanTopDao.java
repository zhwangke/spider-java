package com.dao;

import com.domain.MaoyanTop;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.springframework.jdbc.core.JdbcTemplate;

import java.beans.PropertyVetoException;

/**
 * @Author: WK
 * @Data: 2019/7/15 13:33
 * @Description: com.dao
 */
public class MaoyanTopDao extends JdbcTemplate{
    public MaoyanTopDao() {
        ComboPooledDataSource dataSource = new ComboPooledDataSource();
        try {
            dataSource.setDriverClass("com.mysql.jdbc.Driver");
            dataSource.setJdbcUrl("jdbc:mysql:///movie"); // 需要更改为自己的数据库名称
            dataSource.setUser("root");
            dataSource.setPassword("123"); // 需要更改为自己的mysql的密码
        } catch (PropertyVetoException e) {
            e.printStackTrace();
        }
        super.setDataSource(dataSource);
    }
    public void saveMovie(MaoyanTop maoyanTop) {
        String[] param = {maoyanTop.getTitles(), maoyanTop.getTypes(), maoyanTop.getLocal(), maoyanTop.getTime(), maoyanTop.getInfos(), maoyanTop.getHour(), maoyanTop.getHref()};
        update("INSERT into movie100 VALUES (?,?,?,?,?,?,?)", param);
    }
}