package cn.aezo.utils.base;

import java.io.UnsupportedEncodingException;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by smalle on 2017/11/19.
 */
public class JdbcU {
    private static Pattern FROM_PATTERN = Pattern.compile("(?s)(.*?)([ \t\n\r]+from[ \t\n\r])+(.*?)", Pattern.CASE_INSENSITIVE);

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
     * @date 2016年9月29日 上午8:48:26
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
     * @date 2017年1月4日 下午8:56:44
     */
    // @Deprecated
    public static String getStr4SQLINParam(String[] values, boolean trim){
        List<String> list = Arrays.asList(values);
        return getStr4SQLINParam(list, trim);
    }

    /**
     * 将list转换成sql语句中的in参数(不包含括号). 可使用StrUtil.join()代替，单引号？
     * @param list
     * @param trim 是否对list中的数据执行trim函数
     * @return
     * @author smalle
     * @date 2017年1月4日 下午8:56:21
     */
    // @Deprecated
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
     * @param sql
     * @param pageCurrent 页下标
     * @param pageSize 页长
     * @param returnTotals 是否返回记录总数(返回的字段名为 paging_total__) 注：数据量大的时候最好不要返回
     * @return
     */
    public static String packPageSqlOracle(String sql, Integer pageCurrent, Integer pageSize, Boolean returnTotals) {
        int _pageCurrent = 1;
        int _pageSize = 10;

        if(null != pageCurrent && pageCurrent.compareTo(0) != 0) {
            _pageCurrent = pageCurrent;
        }
        if(null != pageSize && pageSize.compareTo(0) != 0) {
            _pageSize = pageSize;
        }

        int start = (_pageCurrent - 1) * _pageSize + 1;

        if(returnTotals) {
            Matcher m = FROM_PATTERN.matcher(sql);
            while (m.find()) {
                String group = m.group(2);
                sql = sql.replaceFirst(group, ", count(*) over () paging_total__ from ");
                break;
            }
            // sql = sql.replaceFirst(" (?i)from ", ", count(*) over () paging_total__ from ");
        }
        sql = "select * from (select rownum as rn__, paging_t1.* from (" + sql + ") paging_t1 where rownum < " +
                (start + _pageSize) + ") paging_t2 where paging_t2.rn__ >= " + start;
        return sql;
    }

    /**
     * 根据参数length,将String类型对象，进行截取
     * 用于将长字符串，存入数据库中
     * 避免过长 数据库保存失败
     * 避免直接写死长度 产生不必要数据
     * 前提：一个汉字 占3个字节
     *      一个英文 占1个字节
     * @param content 需要截取的字符串
     * @param lengthMax 数据库中存储的最大长度
     * @param lengthCN 汉字占的字节数
     * @return List<String>
     */
    public static String getHeadStr(String content, int lengthMax, int lengthCN) {
        List<String> resultList = new ArrayList<>();
        if (ValidU.isEmpty(content) || lengthCN <= 0 || (lengthMax <= lengthCN)) {
            return "";
        }
        try {
            while(true) {
                //最好情况：content即使都是中文，也 <= lengthMax
                if (content.length() <= lengthMax / lengthCN) {
                    resultList.add(content);
                    break;
                }
                //有超长的可能
                else {
                    int lenStart = 0;
                    //截取到lengthMax / lengthCN，计算总长度
                    for (int i = 0; i < lengthMax / lengthCN; i++) {
                        //获取每个c的长度+++
                        String c = content.substring(i,i+1);
                        lenStart += c.getBytes("UTF-8").length;
                    }
                    StringBuilder builder = new StringBuilder(content.substring(0, (lengthMax / lengthCN)));
                    //循环:当达到最大能储存的最大值 或者 剩下的content取完
                    int i = lengthMax / lengthCN;
                    while (lenStart <= lengthMax && i < content.length()) {
                        String c = content.substring(i,i+1);
                        lenStart += c.getBytes("UTF-8").length;
                        builder.append(c);
                        i++;
                    }
                    //应清楚：当因为达到上限跳出循环时，while循环中的所有操作都是多余的
                    // 包括：i++ -> bug01
                    // 包括：builder最后一次append -> bug02
                    //为何退出循环？
                    // 1：加到content结束，没有达到lengthMax->直接add到resultList
                    // 2：达到了上限->content被赋值成后半段
                    if (lenStart <= lengthMax) {
                        resultList.add(content);
                        break;
                    }else {
                        //bug01
                        content = content.substring(i-1);
                        String line = builder.toString();
                        //bug02
                        resultList.add(line.substring(0,line.length()-1));
                    }
                }
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return resultList.get(0);
    }
}
