import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;


class MultiThreadQueueTest {

    MultiThreadQueue<Integer> queue;
    ExecutorService consumersRunner;
    ExecutorService producersRunner;

    @BeforeEach
    void startUp() throws Exception {
        queue = new MultiThreadQueue<Integer>();
        consumersRunner = Executors.newFixedThreadPool(4);
        producersRunner = Executors.newFixedThreadPool(4);
    }

    void tearDown() {
        consumersRunner.shutdown();
    }

    @Test
    @SneakyThrows
    void queueTest_equalAmount() {
        final AtomicLong sumCounter = new AtomicLong();
        final int elements = 10000;
        List<Runnable> consumersTasks = new ArrayList<>();
        List<Runnable> producersTasks = new ArrayList<>();
        for (int i = 1; i <= elements; i++) {
            final int j = i;
            consumersTasks.add(new Runnable() {
                @Override
                @SneakyThrows
                public void run() {
                    System.out.println(Thread.currentThread().getName() + " trying to consume");
                    var n = queue.poll();
                    System.out.println(Thread.currentThread().getName() + " consumed: " + n);
                    sumCounter.addAndGet(n);
                }
            });

            producersTasks.add(new Runnable() {
                @Override
                @SneakyThrows
                public void run() {

                    System.out.println(Thread.currentThread().getName() + " produced: " + j);
                    queue.offer(j);
                }
            });
        }

        Collections.shuffle(consumersTasks);
        Collections.shuffle(producersTasks);
        consumersTasks.forEach(consumersRunner::execute);
        producersTasks.forEach(producersRunner::execute);
        //tasks.forEach(Runnable::run); // single Thread
        long expected = elements * (elements + 1) / 2;
        while(sumCounter.get() < expected) { //todo countdown latch переписать
            Thread.sleep(1000);
        }
        var actual = sumCounter.get();
        assertEquals(expected, actual);
        //LinkedBlockingQueue // сюда собирать
    }


}
