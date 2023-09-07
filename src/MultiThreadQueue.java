import java.util.concurrent.atomic.AtomicInteger;

public class MultiThreadQueue<T> {

    private final int capacity;
    private final AtomicInteger count;

    private final Object putLock = new Object();
    private final Object getLock = new Object();

    private Node<T> head;
    private Node<T> tail;

    private static class Node<T> {
        private T value;
        private Node<T> next;

        Node(T value) {
            this.value = value;
        }
    }

    private void enqueue(Node<T> node) {
        tail = tail.next = node;
    }

    private T dequeue() {
        head = head.next;
        T t = head.value;
        head.value = null;
        return t;
    }

    private void releaseGetLock() {
        synchronized (getLock) {
            getLock.notify();
        }
    }

    private void releasePutLock() {
        synchronized (putLock) {
            putLock.notify();
        }
    }

    MultiThreadQueue(int size) throws IllegalArgumentException {
        if (size != 0) {
            capacity = size;
            count = new AtomicInteger(0);
        }
        else {
            throw new IllegalArgumentException("Limit must be 1 or bigger");
        }
    }

    MultiThreadQueue() throws Exception {
        this(10);
    }

    public void offer(T value) throws InterruptedException {
        final int c;
        synchronized (putLock) {
            while (count.get() == capacity) {
                putLock.wait();
            }
            enqueue(new Node<>(value));
            c = count.incrementAndGet();
            if (c < capacity)
                putLock.notify();
        }
        if (capacity == 1)
            releaseGetLock();
    }

    public T poll() throws InterruptedException {
        final T value;
        synchronized (getLock) {
            while (count.get() == 0) {
                getLock.wait();
            }
            value = dequeue();
            count.decrementAndGet();
            if (count.get() > 0)
                releaseGetLock();
        }
        if (count.get() < capacity)
            releasePutLock();
        return value;
    }
}
