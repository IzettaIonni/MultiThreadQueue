import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class Tests {

    void test() {
        Executor runner = Executors.newFixedThreadPool(4);
        CountDownLatch barrier = new CountDownLatch(100);
        for (int i = 0; i < 10000; i++) {
            var r = new Runnable() {

                @Override
                public void run() {
                    queue.offer(i);
                    barrier.countDown();
                }
            };
            runner.execute(r);
        }
        barrier.await(); // для poll

        LinkedBlockingQueue // сюда собирать

    }

}
