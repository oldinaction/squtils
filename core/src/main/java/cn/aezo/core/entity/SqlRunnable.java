package cn.aezo.core.entity;

import java.util.List;
import java.util.Map;

/**
 * Created by smalle on 2017/9/8.
 * sql语句执行器
 */
public interface SqlRunnable {
    /**
     * 执行sql语句
     * @param sql sql语句
     * @param params 查询条件
     * @return
     */
    List<Map<String, Object>> runSql(String sql, Map<String, Object> params);

    /**
     * 执行sql语句
     * @param sql sql语句
     * @return
     */
    List<Map<String, Object>> runSql(String sql);
}
