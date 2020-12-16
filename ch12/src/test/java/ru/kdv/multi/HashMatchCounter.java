package ru.kdv.multi;

import lombok.SneakyThrows;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HashMatchCounter {
    private static final ExecutorService pool
            = Executors.newCachedThreadPool();

    public static void main(String[] args) {
        for (int i = 0; i < 100; i++) {
            pool.submit(new MatchCounter(i));
        }
        pool.shutdown();
    }

    private static class MatchCounter implements Runnable{
        private Random rand = new Random();
        private long counter = 0;
        private final int counterName;

        public MatchCounter(int name) {
            counterName = name;
        }

        @SneakyThrows
        @Override
        public void run() {
            int temp = new Random().nextInt();

            while (true){
                int tryMatch = rand.nextInt();
                if ( tryMatch == temp) {
                    System.out.printf("For MatchCounter %d attempt = %d \n", counterName, counter);
                    break;
                }
                counter++;
            }
        }
    }
}
