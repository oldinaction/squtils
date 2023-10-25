package cn.aezo.utils.base;

import java.text.NumberFormat;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TestU {
    public static void multiThreadSimple(MultiThreadSimpleTemplate.Exec exec) {
        new MultiThreadSimpleTemplate().run(exec, null, null);
    }

    public static void multiThreadSimple(MultiThreadSimpleTemplate.Exec exec, int totalNum, int threadNum) {
        new MultiThreadSimpleTemplate().run(totalNum, threadNum, exec, null, null);
    }

    public static void multiThreadSimple(MultiThreadSimpleTemplate.Exec exec, MultiThreadSimpleTemplate.BeforeExec beforeExec,
                                         MultiThreadSimpleTemplate.AfterExec afterExec, int totalNum, int threadNum) {
        new MultiThreadSimpleTemplate().run(totalNum, threadNum, exec, beforeExec, afterExec);
    }

    private static class MultiThreadSimpleTemplate {
        // 总访问量是totalNum，并发量是threadNum
        private int totalNum = 1000;
        private int threadNum = 10;

        private int count = 0;
        private float sumExecTime = 0;
        private long firstExecTime = Long.MAX_VALUE;
        private long lastDoneTime = Long.MIN_VALUE;

        public void run(int totalNum, int threadNum, Exec exec, BeforeExec beforeExec, AfterExec afterExec) {
            this.totalNum = totalNum;
            this.threadNum = threadNum;
            this.run(exec, beforeExec, afterExec);
        }

        public void run(Exec exec, BeforeExec beforeExec, AfterExec afterExec) {
            if(beforeExec != null) {
                if(!beforeExec.beforeExec()) {
                    System.out.println("BeforeExec返回false, 中断运行");
                }
            }

            final ConcurrentHashMap<Integer, ThreadRecord> records = new ConcurrentHashMap<Integer, ThreadRecord>();

            // 建立ExecutorService线程池，threadNum个线程可以同时访问
            ExecutorService es = Executors.newFixedThreadPool(threadNum);
            final CountDownLatch doneSignal = new CountDownLatch(totalNum); // 此数值和循环的大小必须一致

            for (int i = 0; i < totalNum; i++) {
                Runnable run = () -> {
                    try {
                        int index = ++count;
                        long systemCurrentTimeMillis = System.currentTimeMillis();

                        exec.exec();

                        records.put(index, new ThreadRecord(systemCurrentTimeMillis, System.currentTimeMillis()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        // 每调用一次countDown()方法，计数器减1
                        doneSignal.countDown();
                    }
                };
                es.execute(run);
            }

            try {
                // 计数器大于0时，await()方法会阻塞程序继续执行。直到所有子线程完成(每完成一个子线程，计数器-1)
                doneSignal.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // 获取每个线程的开始时间和结束时间
            for (int i : records.keySet()) {
                ThreadRecord r = records.get(i);
                sumExecTime += ((double) (r.endTime - r.startTime)) / 1000;

                if (r.startTime < firstExecTime) {
                    firstExecTime = r.startTime;
                }
                if (r.endTime > lastDoneTime) {
                    this.lastDoneTime = r.endTime;
                }
            }

            float avgExecTime = this.sumExecTime / records.size();
            float totalExecTime = ((float) (this.lastDoneTime - this.firstExecTime)) / 1000;
            NumberFormat nf = NumberFormat.getNumberInstance();
            nf.setMaximumFractionDigits(4);

            // 需要关闭，否则JVM不会退出。(如在Springboot项目的Job中切勿关闭)
            es.shutdown();

            System.out.println("======================================================");
            System.out.println("线程数量:\t" + threadNum + " 个");
            System.out.println("总访问量:\t" + totalNum + " 次");
            System.out.println("平均执行时间:\t" + nf.format(avgExecTime) + " 秒");
            System.out.println("总执行时间:\t" + nf.format(totalExecTime) + " 秒");
            System.out.println("吞吐量:\t\t" + nf.format(totalNum / totalExecTime) + " 次/秒");
            System.out.println("======================================================");

            if(afterExec != null) {
                afterExec.afterExec();
            }
        }

        private static class ThreadRecord {
            long startTime;
            long endTime;

            ThreadRecord(long st, long et) {
                this.startTime = st;
                this.endTime = et;
            }
        }

        @FunctionalInterface
        public interface BeforeExec {
            boolean beforeExec();
        }

        @FunctionalInterface
        public interface Exec {
            void exec();
        }

        @FunctionalInterface
        public interface AfterExec {
            void afterExec();
        }
    }
}


