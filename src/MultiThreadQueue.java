import java.util.List;

public class MultiThreadQueue {

    private final int limit;
    private int[] queue;
    private boolean isLimitReached;

    MultiThreadQueue(int limit) throws Exception {
        if (limit != 0) {
            this.limit = limit;
           isLimitReached = false;
           queue = new int[limit];
        }
        else {
            throw new Exception("Limit must be 1 or bigger");
        }
    }

    MultiThreadQueue() throws Exception {
        this(10);
    }

    public synchronized int get() {

    }

    public synchronized void put(int n) throws Exception {
        while(isLimitReached) {
            try {
                wait();
            } catch (InterruptedException e) {
                throw new Exception("Limit wait is interrupted");
            }
        }
        
    }

}
