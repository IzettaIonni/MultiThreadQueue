import java.util.InvalidPropertiesFormatException;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class MultiThreadQueue<T> {

    private final int limit;
    private boolean isLimitReached;
    private boolean isQueueEmpty;
    private AtomicInteger actual;

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
            this.limit = limit;
            isLimitReached = false;
            isQueueEmpty = true;
            actual = new AtomicInteger(0);
        }
        else {
            throw new Exception("Limit must be 1 or bigger");
        }
    }

    MultiThreadQueue() throws Exception {
        this(10);
    }

    public void offer(T obj) {

        Node<T> next = new Node<T>(obj);

        synchronized (putLock) {

            if (head == null) {
                synchronized (putLock) {
                    if (head == null) head = tail = next;
                }
            }
            else {
                tail.setNext(next);
                tail = next;
            }
        }

    }

    public T poll() {
        if (head == null) {
            return null;
        }
        else {
            T value = head.value;
            head = head.next;
            if (head == null) {
                tail = null;
            }
            return value;
        }

    }

    public  int get() throws Exception {
        synchronized (getLock) {
            System.out.println("---actual--- " + actual);
            if (actual.get() == 0) isQueueEmpty = true;
            while (isQueueEmpty) {
                try {
                    getLock.wait();
                } catch (InterruptedException e) {
                    throw new Exception("Empty wait is interrupted");
                }
            }
            int x = queue[front];

            front = (front + 1) % limit;
            actual.decrementAndGet();

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
