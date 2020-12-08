package ru.kdv.multi;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.fail;

public class InterruptedBoundedBufferTest {
    private static final long LOCKUP_DETECT_TIMEOUT = 1000L;

    @Test
    void testTakeBlocksWhenEmpty() {
        final BoundedBuffer<Integer> bb = new BoundedBuffer<Integer>(10);
        Thread taker = new Thread(() -> {
            try {
                int unused = bb.take();
                fail(); // если мы добрались сюда, то значит ошибка
            } catch (InterruptedException success) { }
        });
        try {
            taker.start();
            Thread.sleep(LOCKUP_DETECT_TIMEOUT);
            taker.interrupt();
            taker.join(LOCKUP_DETECT_TIMEOUT);
            assertFalse(taker.isAlive());
        } catch (Exception unexpected) {
            fail();
        }
    }

    @Test
    void testTakeBlocksWhenNotEmpty() {
        final BoundedBuffer<Integer> bb = new BoundedBuffer<Integer>(10);
        Thread taker = new Thread(() -> {
            try {
                int unused = bb.take();
            } catch (InterruptedException success) { }
        });
        try {
            bb.put(1);
            taker.start();
            assertFalse(bb.isFull());
        } catch (Exception unexpected) {
            fail();
        }
    }
}
