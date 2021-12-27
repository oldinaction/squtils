package cn.aezo.utils.func;

import java.util.List;

/**
 * @author smalle
 * @since 2021-04-14
 */
@FunctionalInterface
public interface TaskListRunnerFunc<E> {
    void run(List<E> list);
}
