public class Consumer implements Runnable{
    private MultiThreadQueue queue;
    private Thread thread;

    Consumer(MultiThreadQueue queue) {
        this.queue = queue;
        thread = new Thread(this, "Consumer");
    }

    @Override
    public void run() {
        while (true) {
            try {
                System.out.println("Consumed: " + queue.get());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void start() {
        thread.start();
    }
}
