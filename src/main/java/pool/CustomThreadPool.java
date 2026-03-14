package pool;

import pool.balancing.*;
import pool.queue.TaskQueue;
import pool.rejection.*;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class CustomThreadPool implements CustomExecutor {

    private final int corePoolSize;
    private final int maxPoolSize;
    private final int minSpareThreads;

    private final long keepAliveTime;
    private final TimeUnit timeUnit;

    private final TaskQueue[] queues;
    private final Thread[] workerThreads;

    private final AtomicInteger workerCount = new AtomicInteger();

    private final TaskBalancer balancer = new RoundRobinBalancer();

    private final RejectionPolicy rejectionPolicy =
            new CallerRunsPolicy();

    private final CustomThreadFactory threadFactory =
            new CustomThreadFactory("MyPool");

    private volatile boolean shutdown = false;

    public CustomThreadPool(
            int corePoolSize,
            int maxPoolSize,
            int queueSize,
            int minSpareThreads,
            long keepAliveTime,
            TimeUnit timeUnit
    ) {

        this.corePoolSize = corePoolSize;
        this.maxPoolSize = maxPoolSize;
        this.minSpareThreads = minSpareThreads;

        this.keepAliveTime = keepAliveTime;
        this.timeUnit = timeUnit;

        this.queues = new TaskQueue[maxPoolSize];
        this.workerThreads = new Thread[maxPoolSize];

        for (int i = 0; i < corePoolSize; i++) {
            createWorker(i, queueSize);
        }
    }

    private synchronized void createWorker(int index, int queueSize) {

        if (workerCount.get() >= maxPoolSize) return;

        TaskQueue queue = new TaskQueue(queueSize, index);

        queues[index] = queue;

        Worker worker = new Worker(queue, this);

        Thread thread = threadFactory.newThread(worker);

        workerThreads[index] = thread;

        workerCount.incrementAndGet();

        thread.start();
    }

    @Override
    public void execute(Runnable task) {

        if (shutdown) {

            rejectionPolicy.reject(task);
            return;
        }

        TaskQueue queue =
                balancer.selectQueue(queues, workerCount.get());

        boolean accepted = queue.offer(task);

        if (!accepted) {

            if (workerCount.get() < maxPoolSize) {

                createWorker(workerCount.get(), queue.remainingCapacity());

                execute(task);

            } else {

                rejectionPolicy.reject(task);
            }

        }

        maintainSpareThreads();
    }

    private void maintainSpareThreads() {

        int idle = 0;

        for (int i = 0; i < workerCount.get(); i++) {

            if (queues[i].isEmpty()) {
                idle++;
            }

        }

        if (idle < minSpareThreads &&
                workerCount.get() < maxPoolSize) {

            createWorker(workerCount.get(),
                    queues[0].remainingCapacity());
        }
    }

    @Override
    public <T> Future<T> submit(Callable<T> callable) {

        FutureTask<T> task = new FutureTask<>(callable);

        execute(task);

        return task;
    }

    @Override
    public void shutdown() {

        System.out.println("[Pool] Graceful shutdown initiated");

        shutdown = true;
    }

    @Override
    public void shutdownNow() {

        System.out.println("[Pool] Immediate shutdown initiated");

        shutdown = true;

        for (Thread t : workerThreads) {

            if (t != null) {

                t.interrupt();
            }
        }
    }

    public boolean shouldTerminateWorker() {

        return workerCount.get() > corePoolSize;
    }

    public boolean isShutdown() {
        return shutdown;
    }

    public long getKeepAliveTime() {
        return keepAliveTime;
    }

    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

    public void workerTerminated() {

        workerCount.decrementAndGet();
    }
}