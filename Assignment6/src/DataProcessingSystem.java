import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class DataProcessingSystem {
    private static final int NUM_WORKERS = 3;
    private static final int NUM_TASKS = 10;

    public static void main(String[] args) {
        // Shared task queue & results list
        Queue<String> taskQueue = new LinkedList<>();
        Queue<String> resultQueue = new LinkedList<>();

        // Lock for thread-safe queue access
        Lock queueLock = new ReentrantLock();

        // Populate task queue
        for (int i = 1; i <= NUM_TASKS; i++) {
            taskQueue.add("Task-" + i);
        }

        // Create a thread pool
        ExecutorService executor = Executors.newFixedThreadPool(NUM_WORKERS);

        // Start worker threads
        for (int i = 0; i < NUM_WORKERS; i++) {
            executor.execute(new Worker(taskQueue, resultQueue, queueLock, "Worker-" + (i + 1)));
        }

        // Shutdown executor when done
        executor.shutdown();
    }
}

class Worker implements Runnable {
    private final Queue<String> taskQueue;
    private final Queue<String> resultQueue;
    private final Lock queueLock;
    private final String workerName;

    public Worker(Queue<String> taskQueue, Queue<String> resultQueue, Lock queueLock, String workerName) {
        this.taskQueue = taskQueue;
        this.resultQueue = resultQueue;
        this.queueLock = queueLock;
        this.workerName = workerName;
    }

    @Override
    public void run() {
        while (true) {
            String task = null;

            // Lock the queue to safely retrieve a task
            queueLock.lock();
            try {
                if (taskQueue.isEmpty()) {
                    System.out.println(workerName + ": No more tasks. Exiting.");
                    break;
                }
                task = taskQueue.poll();
                System.out.println(workerName + ": Processing " + task);
            } finally {
                queueLock.unlock();
            }

            // Simulate processing delay
            try {
                Thread.sleep((long) (Math.random() * 1000));
            } catch (InterruptedException e) {
                System.err.println(workerName + ": Interrupted while processing " + task);
                Thread.currentThread().interrupt();
                return;
            }

            // Store the result (with synchronization)
            queueLock.lock();
            try {
                String result = task + "-COMPLETED";
                resultQueue.add(result);
                System.out.println(workerName + ": Finished " + task + " → " + result);
            } finally {
                queueLock.unlock();
            }
        }
    }
}