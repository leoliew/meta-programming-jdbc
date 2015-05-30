package com.factory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * 数据库管理类
 * 
 * @author Zeming
 * 
 */
public class DBManager {

    private static Connection conn;
    private static String url;
    private static String user;
    private static String password;

    // 获取Connection
    public static Connection getConnection() {
        if(url==null||user==null||password==null){
            initData();
        }
        try {
            if (conn == null || conn.isClosed()) {
                MysqlDataSource ds_mysql = new MysqlDataSource();
                ds_mysql.setUrl(url);// 设置连接字符串
                conn = ds_mysql.getConnection(user, password);
                return conn;
            }
        } catch (SQLException e) {
            System.out.println("连接JDBC出错!");
            e.printStackTrace();
            return null;
        }
        return conn;
    }

    // 关闭资源
    public static void close(Connection conn, PreparedStatement pstm,
            ResultSet rs) {
        try {
            if (rs != null) {
                rs.close();
            }
            if (pstm != null) {
                pstm.close();
            }
            if (conn != null && conn.getAutoCommit() == true) {
                conn.close();
            }
        } catch (SQLException e) {
            System.out.println(" 关闭JDBC 资源 出错 ");
            e.printStackTrace();
        }

    }

    //初始化数据
    public static  void initData() {
        Properties prop = new Properties();
        try {
            // 获取数据源
            InputStream inStream = DBManager.class.getClassLoader().getResourceAsStream("dbconfig.properties");
            prop.load(inStream);
            inStream.close();
            url = prop.getProperty("url");
            user = prop.getProperty("user");
            password = prop.getProperty("password");
        } catch (IOException e) {
             e.printStackTrace();
        }
    }
}
