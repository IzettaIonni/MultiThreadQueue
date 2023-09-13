import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;


class MultiThreadQueueTest {

    MultiThreadQueue<Integer> queue;
    ExecutorService producersRunner;
    ExecutorService consumersRunner;

    @BeforeEach
    void startUp() throws Exception {
        queue = new MultiThreadQueue<Integer>();
        producersRunner = Executors.newFixedThreadPool(4);
        consumersRunner = Executors.newFixedThreadPool(4);
    }

    @AfterEach
    void tearDown() {
        producersRunner.shutdown();
        consumersRunner.shutdown();
    }

    @Test
    @SneakyThrows
    void queueTest_singleThread() {
        long actual = 0;
        final int elements = 10000;
        for (int i = 1; i <= elements; i++) {

            System.out.println(Thread.currentThread().getName() + " produced: " + i);
            queue.offer(i);

            System.out.println(Thread.currentThread().getName() + " trying to consume");
            var n = queue.poll();
            System.out.println(Thread.currentThread().getName() + " consumed: " + n);
            actual += i;

        }
        long expected = elements * (elements + 1) / 2;
        assertEquals(expected, actual);
    }

    @Test
    @SneakyThrows
    void queueTest_equalAmount() {
        final AtomicLong sumCounter = new AtomicLong();
        final int elements = 10000;
        CountDownLatch cdl = new CountDownLatch(elements);
        List<Runnable> producersTasks = new ArrayList<>();
        List<Runnable> consumersTasks = new ArrayList<>();
        for (int i = 1; i <= elements; i++) {
            final int j = i;

            producersTasks.add(new Runnable() {
                @Override
                @SneakyThrows
                public void run() {

                    System.out.println(Thread.currentThread().getName() + " produced: " + j);
                    queue.offer(j);
                }
            });

            consumersTasks.add(new Runnable() {
                @Override
                @SneakyThrows
                public void run() {
                    System.out.println(Thread.currentThread().getName() + " trying to consume");
                    var n = queue.poll();
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

}
