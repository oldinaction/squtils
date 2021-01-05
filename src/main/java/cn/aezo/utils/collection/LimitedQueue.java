package cn.aezo.utils.collection;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 定长队列
 * Created by smalle on 2019-04-10
 */
public class LimitedQueue<E> extends CopyOnWriteArrayList<E> {
    private static final long serialVersionUID = 1L;
    private int limit;

    public LimitedQueue(int limit) {
        this.limit = limit;
    }

    @Override
    public boolean add(E o) {
        super.add(o);
        while (size() > limit) {
            super.remove(0);
        }
        return true;
    }
}