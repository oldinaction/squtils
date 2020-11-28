package cn.aezo.utils.base;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by smalle on 2017/11/19.
 */
public class JdbcU {
    /**
     * ResultSet转成List，Map中的key为数据库字段大写
     * @param rs
     * @return
     * @throws SQLException
     * @author smalle
     * @date 2016年10月23日 下午10:48:45
     */
    public static List<Map<String, Object>> resultSetToList(ResultSet rs) throws SQLException {
        List<Map<String, Object>> list = new ArrayList<>();
        ResultSetMetaData md = rs.getMetaData();
        int columnCount = md.getColumnCount();
        while (rs.next()) {
            Map<String,Object> rowData = new HashMap<>();
            for (int i = 1; i <= columnCount; i++) {
                rowData.put(md.getColumnName(i), rs.getObject(i));
            }
            list.add(rowData);
        }
        return list;
    }

    /**
     * 关闭数据库连接
     * @param rs
     * @param st
     * @param conn
     * @author smalle
     * @date 2016年9月29日 上午9:48:26
     */
    public static void close(ResultSet rs, Statement st, Connection conn) {
        try {
            if (rs != null) {
                rs.close();
                rs = null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (st != null) {
                    st.close();
                    st = null;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (conn != null) {
                        conn.close();
                        conn = null;
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 将数组转换层sql语句中的in参数(不包含括号). 可使用StrUtil.join()代替
     * @param values
     * @param trim 是否对数组中的数据执行trim函数
     * @return
     * @author smalle
     * @date 2017年1月4日 下午4:56:44
     */
    @Deprecated
    public static String getStr4SQLINParam(String[] values, boolean trim){
        List<String> list = Arrays.asList(values);
        return getStr4SQLINParam(list, trim);
    }

    /**
     * 将list转换成sql语句中的in参数(不包含括号). 可使用StrUtil.join()代替
     * @param list
     * @param trim 是否对list中的数据执行trim函数
     * @return
     * @author smalle
     * @date 2017年1月4日 下午4:56:21
     */
    @Deprecated
    public static String getStr4SQLINParam(List<String> list, boolean trim) {
        if(trim) {
            List<String> tempList = new ArrayList<>();
            for (String str : list) {
                tempList.add(str.trim());
            }
            list = tempList;
        } else {
            List<String> tempList = new ArrayList<>();
            for (String str : list) {
                tempList.add(str);
            }
            list = tempList;
        }
        return list.toString().replace("[", "'").replace(", ", "', '").replace("]", "'").replace("{", "'").replace("}", "'");
    }

    /**
     * 生成Oracle的分页sql语句
     * @param sql <b>sql语句的第一个from需要小写</b>
     * @param pageIndex 页下标
     * @param pagingLength 页长
     * @param returnTotals 是否返回记录总数(返回的字段名为 paging_total__) 注：数据量大的时候最好不要返回
     * @return
     */
    public static String pagingSqlOracle(String sql, Integer pageIndex, Integer pagingLength, Boolean returnTotals) {
        int _pageIndex = 1;
        int _pagingLength = 10;

        if(null != pageIndex && pageIndex.compareTo(0) != 0) {
            _pageIndex = pageIndex;
        }
        if(null != pagingLength && pagingLength.compareTo(0) != 0) {
            _pagingLength = pagingLength;
        }

        int start = (_pageIndex - 1) * _pagingLength + 1;

        if(returnTotals) {
            sql = sql.replaceFirst(" from ", ", count(*) over () paging_total__ from ");
        }
        sql = new StringBuffer("select * from (select rownum as rn__, paging_t1.* from (")
                .append(sql)
                .append(") paging_t1 where rownum < ")
                .append(start + _pagingLength)
                .append(") paging_t2 where paging_t2.rn__ >= ")
                .append(start)
                .toString();
        return sql;
    }

}
