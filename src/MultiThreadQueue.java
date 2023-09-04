import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class MultiThreadQueue {

    private final int limit;
    private int[] queue;
    private boolean isLimitReached;
    private boolean isQueueEmpty;
    private int front;
    private int rear;
    private AtomicInteger actual;

    private final Object putLock = new Object();
    private final Object getLock = new Object();

    MultiThreadQueue(int limit) throws Exception {
        if (limit != 0) {
            this.limit = limit;
            isLimitReached = false;
            isQueueEmpty = true;
            queue = new int[limit];
            front = 0;
            rear = -1;
            actual = 0;
        }
        else {
            throw new Exception("Limit must be 1 or bigger");
        }
    }

    MultiThreadQueue() throws Exception {
        this(9);
    }

    public  int get() throws Exception {
        synchronized (getLock) {
            System.out.println("---actual--- " + actual);
            if (actual == 0) isQueueEmpty = true;
            while (isQueueEmpty) {
                try {
                    getLock.wait();
                } catch (InterruptedException e) {
                    throw new Exception("Empty wait is interrupted");
                }
            }
            int x = queue[front];

            front = (front + 1) % limit;
            actual--;

            isLimitReached = false;
            getLock.notify();

            return x;
        }
    }

    public synchronized void put(int n) throws Exception {
        if (actual >= limit) isLimitReached = true;
        while (isLimitReached) {
            try {
                wait();
            } catch (InterruptedException e) {
                throw new Exception("Limit wait is interrupted");
            }
        }
        rear = (rear + 1) % limit;
        queue[rear] = n;
        actual++;

        isQueueEmpty = false;
        notify();
    }

}
