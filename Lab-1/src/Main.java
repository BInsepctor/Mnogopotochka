import java.util.Random;
import java.util.Stack;

public class Main {
    public static void main(String[] args) {
        Object lock = new Object();

        TestOne thread1 = new TestOne();
        TestTwo thred2 = new TestTwo();

        thread1.run();
        thred2.run();

        Thread threadTimer = new Thread(() -> {
            long startTime = System.currentTimeMillis();
            while (true) {
                long currentTimr = System.currentTimeMillis();
                long seconds = (currentTimr - startTime) / 1000;

                System.out.println(" seconds have passed: " + seconds);

                synchronized (lock) {
                    lock.notifyAll();
                }

                try {
                    long sleepTime = 1000 - ((System.currentTimeMillis() - startTime) % 1000);
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        Thread threadFive = new Thread(() -> {
            int seconds = 0;
            while (true) {
                synchronized (lock) {
                    try {
                        lock.wait();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }

                seconds++;

                if (seconds % 5 == 0) {
                    System.out.println("Message 5 sec");
                }
            }
        });

        Thread threadSeven = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(7000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                System.out.println("Message 7 sec");
            }
        });

        threadTimer.start();
        threadFive.start();
        threadSeven.start();


        Object newLock = new Object();
        BoundedStack<Integer> storage = new BoundedStack<>(4);
        Random random = new Random();


        Thread maker = new Thread(() -> {
            while (true) {
                synchronized (newLock) {
                    try {
                        if (storage.size() == 4) {
                            System.out.println("Stack is full");
                            newLock.wait();
                        }

                        int rand = random.nextInt(100);
                        storage.push(rand);
                        System.out.println("Added: " + rand);

                        Thread.sleep(100);
                        newLock.notifyAll();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });


        Thread consumer = new Thread(() -> {
            while (true) {
                synchronized (newLock) {
                    try {
                        if (storage.isEmpty()) {
                            System.out.println("Stack is empty");
                            newLock.wait();
                        }

                        int item = storage.pop();
                        System.out.println("Deleted: " + item);

                        Thread.sleep(150);
                        newLock.notifyAll();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });


        maker.start();
        consumer.start();
    }


}

class TestOne extends Thread{

    @Override
    public void run(){

        for (int i = 0; i <= 100; i++){
            if (i % 10 == 0){
                System.out.println("Thread: "+ i);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }

    }

}

class TestTwo implements Runnable{

    @Override
    public void run() {
        for (int i = 0; i <= 100; i++){
            if (i % 10 == 0){
                System.out.println("Runnable : "+ i);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}

class BoundedStack<T> {
    private final Stack<T> stack = new Stack<>();
    private final int maxSize;

    public BoundedStack(int maxSize) {
        this.maxSize = maxSize;
    }

    public synchronized void push(T item) throws InterruptedException {

        while (stack.size() == maxSize) {
            wait();
        }
        stack.push(item);
        notifyAll();
    }

    public synchronized T pop() throws InterruptedException {

        while (stack.isEmpty()) {
            wait();
        }
        T item = stack.pop();
        notifyAll();
        return item;
    }

    public synchronized boolean isEmpty() {
        return stack.isEmpty();
    }

    public synchronized int size() {
        return stack.size();
    }
}

