package com.attendance.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.*;
import java.sql.*;

@Configuration
public class DataSourceConfig {

    @Value("${spring.datasource.url}")
    private String url;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    @Value("${spring.datasource.driver-class-name}")
    private String driverClassName;

    @PostConstruct
    public void init() {
        try {
            Class.forName(driverClassName);
            Connection conn = DriverManager.getConnection(url, username, password);
            
            InputStream is = getClass().getResourceAsStream("/init.sql");
            if (is != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                StringBuilder sqlBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sqlBuilder.append(line);
                }
                reader.close();
                
                String[] sqlStatements = sqlBuilder.toString().split(";");
                Statement stmt = conn.createStatement();
                for (String sql : sqlStatements) {
                    sql = sql.trim();
                    if (!sql.isEmpty()) {
                        stmt.execute(sql);
                    }
                }
                stmt.close();
                System.out.println("[INFO] 数据库初始化脚本执行完成");
            }
            
            conn.close();
        } catch (Exception e) {
            System.err.println("[WARN] 数据库初始化脚本执行失败: " + e.getMessage());
        }
    }
}