public class Main {



    public static void main(String[] args) throws Exception {
        MultiThreadQueue Q = new MultiThreadQueue();
        Producer p1 = new Producer(Q);
        Producer p2 = new Producer(Q);
        Producer p3 = new Producer(Q);
//        Producer p4 = new Producer(Q);
        Consumer c1 = new Consumer(Q);

        p1.start();
        p2.start();
        p3.start();
//        p4.start();
        c1.start();
    }
}