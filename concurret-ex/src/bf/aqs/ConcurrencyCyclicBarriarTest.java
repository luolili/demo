package bf.aqs;

import java.util.concurrent.*;

/**
 * 用代码进行压测
 */
public class ConcurrencyCyclicBarriarTest {
    // 达到5个才继续
    static CyclicBarrier cyclicBarrier = new CyclicBarrier(5);
    // 线程和数
    public static final int threadCount = 20;

    public static void main(String[] args) throws Throwable {
        ExecutorService exec = Executors.newCachedThreadPool();
        for (int i = 0; i < threadCount; i++) {
            Thread.sleep(1000);
            final int threadNum = i;
            exec.execute(() -> {
                try {
                    race(threadNum);
                } catch (Exception e) {
                    System.out.println("exeception");
                }
            });

        }
        exec.shutdown();
        // 最后执行
        System.out.println("finish");

    }

    public static void race(int threadNum) {
        try {
            Thread.sleep(100);
        } catch (Exception e) {

        }
        System.out.println(" ready thread num:" + threadNum);
        try {
            cyclicBarrier.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (BrokenBarrierException e) {
            e.printStackTrace();
        }
        System.out.println("continue--" + threadNum);
    }
}
