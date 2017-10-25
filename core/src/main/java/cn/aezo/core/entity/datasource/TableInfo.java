package cn.aezo.core.entity.datasource;

import cn.aezo.core.entity.SqlRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by smalle on 2017/9/7.
 */
public abstract class TableInfo {
    private ColumnInfo columnInfo;
    private SqlRunnable sqlRunnable;
    private List<String> searchTableNames;

    public TableInfo(ColumnInfo columnInfo, SqlRunnable sqlRunnable) {
        this.columnInfo = columnInfo;
        this.sqlRunnable = sqlRunnable;
    }

    /**
     * 查询数据库表的SQL
     *
     * @return
     */
    protected abstract String getShowTablesSQL(String dbName);

    /**
     * 构建表信息
     * @param tableMap
     * @return
     */
    protected abstract TableDefinition buildTableDefinition(Map<String, Object> tableMap);

    /**
     * 获取表信息(包括列信息)
     * @param dbName
     * @return
     */
    public List<TableDefinition> getTableDefinitions(String dbName) {
        List<Map<String, Object>> resultList = sqlRunnable.runSql(getShowTablesSQL(dbName));
        List<TableDefinition> tableList = new ArrayList<TableDefinition>(resultList.size());

        for (Map<String, Object> rowMap : resultList) {
            TableDefinition tableDefinition = this.buildTableDefinition(rowMap);
            tableDefinition.setColumnDefinitions(columnInfo.getColumnDefinitions(tableDefinition.getTableName()));
            tableList.add(tableDefinition);
        }

        return tableList;
    }

    /**
     * 获取表基本信息
     * @param dbName
     * @return
     */
    public List<TableDefinition> getSimpleTableDefinitions(String dbName) {
        List<Map<String, Object>> resultList = sqlRunnable.runSql(getShowTablesSQL(dbName));
        List<TableDefinition> tableList = new ArrayList<TableDefinition>(resultList.size());

        for (Map<String, Object> rowMap : resultList) {
            tableList.add(this.buildTableDefinition(rowMap));
        }

        return tableList;
    }

    public ColumnInfo getColumnInfo() {
        return columnInfo;
    }

    public void setColumnInfo(ColumnInfo columnInfo) {
        this.columnInfo = columnInfo;
    }

    public SqlRunnable getSqlRunnable() {
        return sqlRunnable;
    }

    public void setSqlRunnable(SqlRunnable sqlRunnable) {
        this.sqlRunnable = sqlRunnable;
    }

    public List<String> getSearchTableNames() {
        return searchTableNames;
    }

    public void setSearchTableNames(List<String> searchTableNames) {
        this.searchTableNames = searchTableNames;
    }
}
