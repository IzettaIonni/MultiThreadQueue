import java.util.List;
import java.util.Queue;

public class MultiThreadQueue {

    private final int limit;
    private int[] queue;
    private boolean isLimitReached;
    private boolean isQueueEmpty;
    private int front;
    private int rear;
    private int actual;

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

    public synchronized int get() throws Exception {
        System.out.println("---actual--- " + actual);
        if (actual == 0) isQueueEmpty = true;
        while (isQueueEmpty) {
            try {
                wait();
            } catch (InterruptedException e) {
                throw new Exception("Empty wait is interrupted");
            }
        }
        int x = queue[front];

        front = (front + 1) % limit;
        actual--;

        isLimitReached = false;
//        notify();

        return x;
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
