import java.time.Clock;
import java.util.concurrent.LinkedBlockingQueue;
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
        if (size > 0) {
            head = tail = new Node<T>(null);
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

    public void put(T value) throws InterruptedException {
        final int c;
        synchronized (putLock) {
            while (count.get() >= capacity) {
                System.out.println("-----------------offer is locked---------------"); //todo delete
                putLock.wait();
            }
            enqueue(new Node<>(value));
            c = count.incrementAndGet();
            if (c < capacity)
                putLock.notify();
        }
        if (count.get() >= 1)
            releaseGetLock();
    }

    public T take() throws InterruptedException {
        final T value;
        final int c;
        synchronized (getLock) {
            while (count.get() <= 0) {
                System.out.println("-----------------poll is locked---------------"); //todo delete
                getLock.wait();
            }
            value = dequeue();
            c = count.decrementAndGet();
            if (c > 0)
                getLock.notify();
        }
        if (count.get() < capacity)
            releasePutLock();
        return value;
    }

    public boolean offer(T value, long timeoutMillis) throws InterruptedException {
        final int c;
        Clock clock = Clock.systemDefaultZone();
        long timeoutCountdown;
        synchronized (putLock) {
            while (count.get() >= capacity) {
                System.out.println("-----------------offer is locked---------------"); //todo delete
                timeoutCountdown = clock.millis();
                putLock.wait(timeoutMillis);
                if (clock.millis() - timeoutCountdown >= timeoutMillis)
                    return false;
            }
            enqueue(new Node<>(value));
            c = count.incrementAndGet();
            if (c < capacity)
                putLock.notify();
        }
        if (count.get() >= 1)
            releaseGetLock();
        return true;
    }

    public T poll(long timeoutMillis) throws InterruptedException {
        final T value;
        final int c;
        Clock clock = Clock.systemDefaultZone();
        long timeoutCountdown;
        synchronized (getLock) {
            while (count.get() <= 0) {
                System.out.println("-----------------poll is locked---------------"); //todo delete
                timeoutCountdown = clock.millis();
                getLock.wait(timeoutMillis);
                if (clock.millis() - timeoutCountdown >= timeoutMillis)
                    return null;
            }
            value = dequeue();
            c = count.decrementAndGet();
            if (c > 0)
                getLock.notify();
        }
        if (count.get() < capacity)
            releasePutLock();
        return value;
    }

    public boolean offer(T value) {
        if (count.get() >= capacity) {
            return false;
        }
        final int c;
        synchronized (putLock) {
            if (count.get() >= capacity) {
                return false;
            }
            enqueue(new Node<>(value));
            c = count.incrementAndGet();
            if (c < capacity)
                putLock.notify();
        }
        if (count.get() >= 1)
            releaseGetLock();
        return true;
    }

    public T poll() {
        if (count.get() == 0)
            return null;
        final T value;
        final int c;
        synchronized (getLock) {
            if (count.get() == 0)
                return null;
            value = dequeue();
            c = count.decrementAndGet();
            if (c > 0)
                getLock.notify();
        }
        if (count.get() < capacity)
            releasePutLock();
        return value;
    }

}
