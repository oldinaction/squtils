package cn.aezo.utils.base;

/**
 * Created by smalle on 2017/11/19.
 */
public class JdbcU {
    /**
     * 生成Oracle的分页sql语句
     * @param sql <b>sql语句的第一个from需要小写</b>
     * @param pageIndex 页下标
     * @param pagingLength 页长
     * @param returnTotals 是否返回记录总数(返回的字段名为paging_total__) 注：数据量大的时候最好不要返回
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

        if(returnTotals)
            sql = sql.replaceFirst(" from ", ", count(*) over () paging_total__ from ");
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
