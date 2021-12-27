package cn.aezo.utils.base;

import cn.aezo.utils.func.TaskListRunnerFunc;
import cn.hutool.core.util.NumberUtil;

import java.math.RoundingMode;
import java.util.List;

/**
 * @author smalle
 * @since 2021-04-14
 */
public class ThreadU {
    /**
     * 将任务分割成多次执行(无多线程功能)
     * @author smalle
     * @since 2021/4/14
     * @param list 待分割的目标数组
     * @param perNum 每次取出个数，默认100
     * @param taskListRunner 执行逻辑单元
     * @return void
     */
    public static <E> void taskSplit(List<E> list, Integer perNum, TaskListRunnerFunc<E> taskListRunnerFunc) {
        if (ValidU.isEmpty(list)) {
            return;
        }
        if (perNum == null) {
            perNum = 100;
        }
        int total = list.size();
        int count = ((Double) NumberUtil.div(total, perNum.intValue(), 0, RoundingMode.UP)).intValue();
        for (int i = 0; i < count; i++) {
            int start = i * perNum;
            int end = start + perNum;
            if(end > list.size()) {
                end = list.size();
            }
            List<E> listItem = list.subList(start, end);
            taskListRunnerFunc.run(listItem);
        }
    }
}
