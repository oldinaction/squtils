package cn.aezo.core.entity.datasource;

/**
 * Created by smalle on 2017/9/7.
 */
public interface DataSourceInfo {
    /**
     * 获取数据源所有表信息
     * @return
     */
    TableInfo getTableInfo();

    /**
     * 获取数据源所有列信息
     * @return
     */
    ColumnInfo getColumnInfo();
}
