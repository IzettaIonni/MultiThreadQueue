public class Producer implements Runnable{
    private MultiThreadQueue queue;
    private Thread thread;

    Producer(MultiThreadQueue queue) {
        this.queue = queue;
        thread = new Thread(this, "Producer");
    }

    @Override
    public void run() {
        int i = 0;
        while (i < 50) {
            try {
                i++;
                queue.offer(i);
                System.out.println("Produced: " + i);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void start() {
        thread.start();
    }

}
