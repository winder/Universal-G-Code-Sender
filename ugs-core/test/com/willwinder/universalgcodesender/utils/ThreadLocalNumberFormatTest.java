package com.willwinder.universalgcodesender.utils;

import org.junit.Test;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ThreadLocalNumberFormatTest {

    @Test
    public void format_shouldUseOneDelegatePerThread() throws Exception {
        AtomicInteger createdDelegates = new AtomicInteger();
        ThreadLocalNumberFormat numberFormat = new ThreadLocalNumberFormat(() -> {
            createdDelegates.incrementAndGet();
            return new DecimalFormat("#.###");
        });

        numberFormat.format(1.5);
        numberFormat.format(2.5);
        runInNewThread(() -> numberFormat.format(3.5));

        assertEquals(2, createdDelegates.get());
    }

    @Test
    public void format_shouldFormatUsingTheSuppliedNumberFormat() {
        NumberFormat numberFormat = new ThreadLocalNumberFormat(() -> new DecimalFormat("#.###"));

        String result = numberFormat.format(2.34567);

        assertEquals("2.346", result);
    }

    @Test
    public void parse_shouldParseUsingTheSuppliedNumberFormat() throws Exception {
        NumberFormat numberFormat = new ThreadLocalNumberFormat(() -> new DecimalFormat("#.###"));

        Number result = numberFormat.parse("2.346");

        assertEquals(2.346, result.doubleValue(), 0.0001);
    }

    @Test
    public void parse_shouldNotBeAffectedByConcurrentFormatting() throws Exception {
        NumberFormat numberFormat = new ThreadLocalNumberFormat(() -> new DecimalFormat("#.###"));
        AtomicReference<Throwable> failure = new AtomicReference<>();
        CountDownLatch start = new CountDownLatch(1);

        Thread formatting = new Thread(() -> {
            awaitQuietly(start);
            for (int i = 0; i < 200_000; i++) {
                try {
                    numberFormat.format(i * 0.017);
                } catch (Throwable t) {
                    failure.compareAndSet(null, t);
                }
            }
        });
        Thread parsing = new Thread(() -> {
            awaitQuietly(start);
            for (int i = 0; i < 200_000; i++) {
                try {
                    numberFormat.parse("2.5");
                } catch (Throwable t) {
                    failure.compareAndSet(null, t);
                }
            }
        });
        formatting.start();
        parsing.start();
        start.countDown();
        formatting.join();
        parsing.join();

        assertNull(failure.get());
    }

    private static void runInNewThread(Runnable runnable) throws InterruptedException {
        Thread thread = new Thread(runnable);
        thread.start();
        thread.join();
    }

    private static void awaitQuietly(CountDownLatch latch) {
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
