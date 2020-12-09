package ru.kdv.multi;

import org.junit.jupiter.api.Test;

import java.lang.management.ManagementFactory;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class LeakTest {
    private static final int CAPACITY = 1000;
    private static final int THRESHOLD = 1_050_000_000;

    class Big {
        double[] data = new double[100000];
    }

    @Test
    void testLeak() throws InterruptedException {
        BoundedBuffer<Big> bb = new BoundedBuffer<>(CAPACITY);
        /* куча для снимка */
        long heapSize1 = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed();
        for (int i = 0; i < CAPACITY; i++)
            bb.put(new Big());
        for (int i = 0; i < CAPACITY; i++)
            bb.take();
        /* куча для снимка */
        long heapSize2 =  ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed();
        assertTrue(Math.abs(heapSize1 - heapSize2) < THRESHOLD);
    }
}
