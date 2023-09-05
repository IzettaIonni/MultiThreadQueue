import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class MultiThreadQueue<T> {

//    private final int limit;
//    private boolean isLimitReached;
//    private boolean isQueueEmpty;
    private AtomicInteger count;

    private final Object putLock = new Object();
    private final Object getLock = new Object();

    private Node<T> head;
    private Node<T> tail;

    private static class Node<T> {
        private final T value;
        private Node<T> next;

        Node(T value) {
            this.value = value;
        }

        public void setNext(Node<T> next) {
            this.next = next;
        }
    }

    MultiThreadQueue(int limit) throws Exception {
        if (limit != 0) {
//            this.limit = limit;
//            isLimitReached = false;
//            isQueueEmpty = true;
            count = new AtomicInteger(0);
        }
        else {
            throw new Exception("Limit must be 1 or bigger");
        }
    }

    MultiThreadQueue() throws Exception {
        this(10);
    }

//    private void signalNotEmpty() {
//        final ReentrantLock takeLock = this.takeLock;
//        takeLock.lock();
//        try {
//            isQueueEmpty.signal();
//        } finally {
//            takeLock.unlock();
//        }
//    }
//
//    /**
//     * Signals a waiting put. Called only from take/poll.
//     */
//    private void signalNotFull() {
//        final ReentrantLock putLock = this.putLock;
//        putLock.lock();
//        try {
//            .signal();
//        } finally {
//            putLock.unlock();
//        }
//    }

    public void offer(T obj) {
        final Node<T> next = new Node<T>(obj);
        synchronized (putLock) {
            if (head == null) {
                head = tail = next;
            }
            else {
                tail.setNext(next);
                tail = next;
            }
            count.incrementAndGet();
        }
    }

    public T poll() {
        if (head == null) {
            return null;
        }
        synchronized (getLock) {
            if (head == null) {
                return null;
            }
            T value = head.value;
            head = head.next;
            if (head == null) {
                tail = null;
            }
            count.decrementAndGet();
            return value;

            T value = head.next.value;
            head = head.next;
            return value;
        }
    }

    public  int get() throws Exception {
        synchronized (getLock) {
            System.out.println("---actual--- " + count);
            if (count.get() == 0) isQueueEmpty = true;
            while (isQueueEmpty) {
                try {
                    getLock.wait();
                } catch (InterruptedException e) {
                    throw new Exception("Empty wait is interrupted");
                }
            }
            int x = queue[front];

            front = (front + 1) % limit;
            count.decrementAndGet();

            isLimitReached = false;
            getLock.notify();

            return x;
        }
    }

    public synchronized void put(int n) throws Exception {
        if (count >= limit) isLimitReached = true;
        while (isLimitReached) {
            try {
                wait();
            } catch (InterruptedException e) {
                throw new Exception("Limit wait is interrupted");
            }
        }
        rear = (rear + 1) % limit;
        queue[rear] = n;
        count++;

        isQueueEmpty = false;
        notify();
    }

}
