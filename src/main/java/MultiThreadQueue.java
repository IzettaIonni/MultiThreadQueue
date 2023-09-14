import lombok.AllArgsConstructor;
import lombok.NonNull;

import java.time.Clock;
import java.util.concurrent.atomic.AtomicInteger;

@AllArgsConstructor
public class MultiThreadQueue<T> {

    @NonNull
    private final int capacity;
    @NonNull
    private final AtomicInteger count;
    @NonNull
    private final Clock clock;//= Clock.systemDefaultZone();

    @NonNull
    private final Object putLock;//= new Object();
    @NonNull
    private final Object getLock;//= new Object();

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
        if (count.incrementAndGet() < capacity)
            putLock.notify();
    }

    private T dequeue() {
        head = head.next;
        T t = head.value;
        head.value = null;
        if (count.decrementAndGet() > 0)
            getLock.notify();
        return t;
    }

    private void notifyReaders() {
        if (count.get() >= 1) {
            synchronized (getLock) {
                getLock.notify();
            }
        }
    }

    private void notifyWriters() {
        if (count.get() < capacity) {
            synchronized (putLock) {
                putLock.notify();
            }
        }
    }

//    MultiThreadQueue(int size) throws IllegalArgumentException {
//        if (size > 0) {
//            head = tail = new Node<T>(null);
//            capacity = size;
//            count = new AtomicInteger(0);
//        }
//        else {
//            throw new IllegalArgumentException("Limit must be 1 or bigger");
//        }
//    }
//
//    MultiThreadQueue() throws Exception {
//        this(10);
//    }
    private MultiThreadQueue(
        int capacity, AtomicInteger count, Clock clock, Object putLock, Object getLock, Node<T> node) {
    this(capacity, count, clock, putLock, getLock, node, node);
    }

    public MultiThreadQueue(
            int capacity, AtomicInteger count, Clock clock, Object putLock, Object getLock) {
        this(capacity, count, clock, putLock, getLock, new Node<>(null));
    }

    public MultiThreadQueue(int capacity) {
        this(capacity, new AtomicInteger(), Clock.systemDefaultZone(), new Object(), new Object());
    }

    public MultiThreadQueue() {
        this(10);
    }

    public void put(T value) throws InterruptedException {
        synchronized (putLock) {
            while (count.get() >= capacity) {
                putLock.wait();
            }
            enqueue(new Node<>(value));
        }
        notifyReaders();
    }

    public T take() throws InterruptedException {
        final T value;
        synchronized (getLock) {
            while (count.get() <= 0) {
                getLock.wait();
            }
            value = dequeue();
        }
        notifyWriters();
        return value;
    }

    public boolean offer(T value, long timeoutMillis) throws InterruptedException {
        long timeoutCountdown = clock.millis();
        synchronized (putLock) {
            while (count.get() >= capacity) {
                putLock.wait(timeoutMillis);
                if (clock.millis() - timeoutCountdown >= timeoutMillis)
                    return false;
            }
            enqueue(new Node<>(value));
        }
        notifyReaders();
        return true;
    }

    public T poll(long timeoutMillis) throws InterruptedException {
        final T value;
        long timeoutCountdown = clock.millis();
        synchronized (getLock) {
            while (count.get() <= 0) {
                getLock.wait(timeoutMillis);
                if (clock.millis() - timeoutCountdown >= timeoutMillis)
                    return null;
            }
            value = dequeue();
        }
        notifyWriters();
        return value;
    }

    public boolean offer(T value) {
        if (count.get() >= capacity) {
            return false;
        }
        synchronized (putLock) {
            if (count.get() >= capacity) {
                return false;
            }
            enqueue(new Node<>(value));
        }
        notifyReaders();
        return true;
    }

    public T poll() {
        if (count.get() == 0)
            return null;
        final T value;
        synchronized (getLock) {
            if (count.get() == 0)
                return null;
            value = dequeue();
        }
        notifyWriters();
        return value;
    }

}
