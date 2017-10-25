package cn.aezo.core.entity.datasource;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by smalle on 2017/9/7.
 * 数据源配置
 */
public class DBConfig {
    private static Map<String, String> jdbcUrlMap = new HashMap<String, String>();
    static {
        jdbcUrlMap.put("com.mysql.jdbc.Driver", "jdbc:mysql://%s:%s/%s?useUnicode=true&characterEncoding=UTF-8");
        jdbcUrlMap.put("com.microsoft.sqlserver.jdbc.SQLServerDriver", "jdbc:sqlserver://%s:%s;DatabaseName=%s");
    }

    private String ip;
    private int port;
    private String username;
    private String password;
    private String driverClass;
    private String dbName;

    public DBConfig(String ip, int port, String username, String password, String driverClass, String dbName) {
        this.ip = ip;
        this.port = port;
        this.username = username;
        this.password = password;
        this.driverClass = driverClass;
        this.dbName = dbName;
    }

    public String getJdbcUrl() {
        String url = jdbcUrlMap.get(driverClass);
        return String.format(url, ip, port, dbName);
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDriverClass() {
        return driverClass;
    }

    public void setDriverClass(String driverClass) {
        this.driverClass = driverClass;
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }
}
