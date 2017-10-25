package cn.aezo.core.entity.datasource;

import cn.aezo.core.entity.SqlRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by smalle on 2017/9/7.
 */
public abstract class ColumnInfo {
    private SqlRunnable sqlRunnable;

    public ColumnInfo(SqlRunnable sqlRunnable) {
        this.sqlRunnable = sqlRunnable;
    }

    /**
     * 返回查询表字段信息的SQL语句,不同的数据查询表信息不一样
     * 如mysql是DESC tableName
     * @return
     */
    protected abstract String getColumnInfoSQL(String tableName);

    /**
     * 构建列信息
     * @param rowMap
     * @return
     */
    protected abstract ColumnDefinition buildColumnDefinition(Map<String, Object> rowMap);

    /**
     * 获取列的定义
     * @param tableName
     * @return
     */
    public List<ColumnDefinition> getColumnDefinitions(String tableName) {
        List<Map<String, Object>> resultList = sqlRunnable.runSql(getColumnInfoSQL(tableName));

        List<ColumnDefinition> columnDefinitionList = new ArrayList<ColumnDefinition>(resultList.size());
        // 构建columnDefinition
        for (Map<String, Object> rowMap : resultList) {
            columnDefinitionList.add(buildColumnDefinition(rowMap));
        }

        return columnDefinitionList;
    }
}
