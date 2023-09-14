import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import java.time.Clock;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;


class MultiThreadQueueTest {

    MultiThreadQueue<Integer> queue;
    ExecutorService producersRunner;
    ExecutorService consumersRunner;
    long elements;

    @BeforeEach
    void startUp() throws Exception {
        queue = new MultiThreadQueue<Integer>();
        elements = 10000;
    }

    void createRunners(int producers, int consumers) {

    }

    @AfterEach
    void tearDown() {
//        producersRunner.shutdown();
//        consumersRunner.shutdown();
    }

    @Test
    @SneakyThrows
    void queueTest_singleThread() {
        long actual = 0;
        for (int i = 1; i <= elements; i++) {

            System.out.println(Thread.currentThread().getName() + " produced: " + i);
            queue.put(i);

            System.out.println(Thread.currentThread().getName() + " trying to consume");
            var n = queue.take();
            System.out.println(Thread.currentThread().getName() + " consumed: " + n);
            actual += i;

        }
        long expected = elements * (elements + 1) / 2;
        assertEquals(expected, actual);
    }

    @Test
    @SneakyThrows
    void queueTest_equalAmount() {
        producersRunner = Executors.newFixedThreadPool(4);
        consumersRunner = Executors.newFixedThreadPool(4);
        final AtomicLong sumCounter = new AtomicLong();
        CountDownLatch cdl = new CountDownLatch((int) elements);
        List<Runnable> producersTasks = new ArrayList<>();
        List<Runnable> consumersTasks = new ArrayList<>();

        for (int i = 1; i <= elements; i++) {
            final int j = i;

            producersTasks.add(new Runnable() {
                @Override
                @SneakyThrows
                public void run() {

                    System.out.println(Thread.currentThread().getName() + " produced: " + j);
                    queue.put(j);
                }
            });

            consumersTasks.add(new Runnable() {
                @Override
                @SneakyThrows
                public void run() {
                    System.out.println(Thread.currentThread().getName() + " trying to consume");
                    var n = queue.take();
                    System.out.println(Thread.currentThread().getName() + " consumed: " + n);
                    sumCounter.addAndGet(n);
                    cdl.countDown();
                }
            });
        }

        Collections.shuffle(consumersTasks);
        Collections.shuffle(producersTasks);
        producersTasks.forEach(producersRunner::execute);
        consumersTasks.forEach(consumersRunner::execute);
        long expected = elements * (elements + 1) / 2;
        cdl.await();
        var actual = sumCounter.get();
        assertEquals(expected, actual);
    }

    @Test
    @SneakyThrows
    void queueTest_moreProducers() {
        producersRunner = Executors.newFixedThreadPool(7);
        consumersRunner = Executors.newFixedThreadPool(1);
        final AtomicLong sumCounter = new AtomicLong();
        CountDownLatch cdl = new CountDownLatch((int) elements);
        List<Runnable> producersTasks = new ArrayList<>();
        List<Runnable> consumersTasks = new ArrayList<>();

        for (int i = 1; i <= elements; i++) {
            final int j = i;

            producersTasks.add(new Runnable() {
                @Override
                @SneakyThrows
                public void run() {

                    System.out.println(Thread.currentThread().getName() + " produced: " + j);
                    queue.put(j);
                }
            });

            consumersTasks.add(new Runnable() {
                @Override
                @SneakyThrows
                public void run() {
                    System.out.println(Thread.currentThread().getName() + " trying to consume");
                    var n = queue.take();
                    System.out.println(Thread.currentThread().getName() + " consumed: " + n);
                    sumCounter.addAndGet(n);
                    cdl.countDown();
                }
            });
        }

        Collections.shuffle(consumersTasks);
        Collections.shuffle(producersTasks);
        producersTasks.forEach(producersRunner::execute);
        consumersTasks.forEach(consumersRunner::execute);
        long expected = elements * (elements + 1) / 2;
        cdl.await();
        var actual = sumCounter.get();
        assertEquals(expected, actual);
    }

    @Test
    @SneakyThrows
    void queueTest_moreConsumers() {
        producersRunner = Executors.newFixedThreadPool(1);
        consumersRunner = Executors.newFixedThreadPool(7);
        final AtomicLong sumCounter = new AtomicLong();
        CountDownLatch cdl = new CountDownLatch((int) elements);
        List<Runnable> producersTasks = new ArrayList<>();
        List<Runnable> consumersTasks = new ArrayList<>();
        for (int i = 1; i <= elements; i++) {
            final int j = i;

            producersTasks.add(new Runnable() {
                @Override
                @SneakyThrows
                public void run() {

                    System.out.println(Thread.currentThread().getName() + " produced: " + j);
                    queue.put(j);
                }
            });

            consumersTasks.add(new Runnable() {
                @Override
                @SneakyThrows
                public void run() {
                    System.out.println(Thread.currentThread().getName() + " trying to consume");
                    var n = queue.take();
                    System.out.println(Thread.currentThread().getName() + " consumed: " + n);
                    sumCounter.addAndGet(n);
                    cdl.countDown();
                }
            });
        }

        Collections.shuffle(consumersTasks);
        Collections.shuffle(producersTasks);
        producersTasks.forEach(producersRunner::execute);
        consumersTasks.forEach(consumersRunner::execute);
        long expected = elements * (elements + 1) / 2;
        cdl.await();
        var actual = sumCounter.get();
        assertEquals(expected, actual);
    }

    @Test
    @SneakyThrows
    void offerTimeoutTest_singleThread() {
        var expected = Integer.valueOf(77);
        queue.offer(expected, 100);
        var actual = queue.take();
        assertEquals(expected, actual);
    }

    @Test
    @SneakyThrows
    void offerTimeoutTest_fullQueue_singleThread() {
        Clock clock = Clock.systemDefaultZone();
        long expectedTimeout = 100;
        for (int i = 0; i < 10; i++) {
            queue.put(i);
        }
        var actualTimeout = clock.millis();
        var actualValue = queue.offer(1, expectedTimeout);
        actualTimeout = clock.millis() - actualTimeout;
        assertTrue(actualTimeout > expectedTimeout && actualTimeout < expectedTimeout * 2);
        assertFalse(actualValue);
    }

    @Test
    @SneakyThrows
    void pollTimeoutTest_singleThread() {
        var expected = Integer.valueOf(77);
        queue.put(expected);
        var actual = queue.poll(100);
        assertEquals(expected, actual);
    }

    @Test
    @SneakyThrows
    void pollTimeoutTest_emptyQueue_singleThread() {
        Clock clock = Clock.systemDefaultZone();
        long expectedTimeout = 100;
        var actualTimeout = clock.millis();
        var actualValue = queue.poll(expectedTimeout);
        actualTimeout = clock.millis() - actualTimeout;
        assertTrue(actualTimeout > expectedTimeout && actualTimeout < expectedTimeout * 2);
        assertNull(actualValue);
    }

    @Test
    @SneakyThrows
    void offerNoTimeoutTest_singleThread() {
        var expected = Integer.valueOf(77);
        queue.offer(expected);
        var actual = queue.take();
        assertEquals(expected, actual);
    }

    @Test
    @SneakyThrows
    void offerNoTimeoutTest_fullQueue_singleThread() {
        for (int i = 0; i < 10; i++) {
            queue.put(i);
        }
        var actualValue = queue.offer(1);
        assertFalse(actualValue);
    }

    @Test
    @SneakyThrows
    void pollNoTimeoutTest_singleThread() {
        var expected = Integer.valueOf(86);
        queue.put(expected);
        var actual = queue.poll();
        assertEquals(expected, actual);
    }

    @Test
    @SneakyThrows
    void pollNoTimeoutTest_emptyQueue_singleThread() {
        var actualValue = queue.poll();
        assertNull(actualValue);
    }

}
