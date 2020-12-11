package ru.kdv.multi;

import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static ru.kdv.multi.RandGenerator.*;

public class PutTakeTest {
    private static final ExecutorService pool
            = Executors.newCachedThreadPool();
    private final AtomicInteger putSum = new AtomicInteger(0);
    private final AtomicInteger takeSum = new AtomicInteger(0);
    private final CyclicBarrier barrier;
    private final BarrierTimer barrierTimer;
    private final BoundedBuffer<Integer> bb;
    private final int nTrials, nPairs;

    public static void main(String[] args) {
        new PutTakeTest(10, 10, 100000).test(); // образец параметров
        pool.shutdown();
    }

    PutTakeTest(int capacity, int npairs, int ntrials) {
        this.bb = new BoundedBuffer<>(capacity);
        this.nTrials = ntrials;
        this.nPairs = npairs;
        this.barrierTimer = new BarrierTimer();
        this.barrier = new CyclicBarrier(npairs * 2 + 1, barrierTimer);
    }

    void test() {
        try {
            barrierTimer.clear();
            for (int i = 0; i < nPairs; i++) {
                pool.execute(new Producer());
                pool.execute(new Consumer());
            }
            barrier.await(); // ждать, когда все потоки будут готовы
            barrier.await(); // ждать, когда все потоки завершатся
            long nsPerItem = barrierTimer.getTime() / (nPairs * (long) nTrials);
            //System.out.print("Throughput: " + nsPerItem + " ns/item");
            assertEquals(putSum.get(), takeSum.get());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    class Producer implements Runnable {
        public void run() {
            try {
                int seed = (this.hashCode() ^ (int) System.nanoTime());
                int sum = 0;
                barrier.await();
                for (int i = nTrials; i > 0; --i) {
                    bb.put(seed);
                    sum += seed;
                    seed = xorShift(seed);
                }
                putSum.getAndAdd(sum);
                barrier.await();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    class Consumer implements Runnable {
        public void run() {
            try {
                barrier.await();
                int sum = 0;
                for (int i = nTrials; i > 0; --i) {
                    sum += bb.take();
                }
                takeSum.getAndAdd(sum);
                barrier.await();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    class BarrierTimer implements Runnable {
        private boolean started;
        private long startTime, endTime;

        public synchronized void run() {
            long t = System.nanoTime();
            if (!started) {
                started = true;
                startTime = t;
            } else {
                endTime = t;
                long nsPerItem = barrierTimer.getTime() / (nPairs * (long) nTrials);
                System.out.println("Throughput: " + nsPerItem + " ns/item");
            }
        }

        public synchronized void clear() {
            started = false;
        }

        public synchronized long getTime() {
            return endTime - startTime;
        }


    }
}
