package pool;

import pool.queue.TaskQueue;

import java.util.concurrent.TimeUnit;

public class Worker implements Runnable {

    private final TaskQueue queue;

    private final CustomThreadPool pool;

    public Worker(TaskQueue queue, CustomThreadPool pool) {

        this.queue = queue;
        this.pool = pool;
    }

    @Override
    public void run() {

        Thread current = Thread.currentThread();

        try {

            while (true) {

                if (pool.isShutdown() && queue.isEmpty()) {
                    break;
                }

                Runnable task = queue.poll(
                        pool.getKeepAliveTime(),
                        pool.getTimeUnit()
                );

                if (task == null) {

                    if (pool.shouldTerminateWorker()) {

                        System.out.println(
                                "[Worker] " + current.getName() +
                                        " idle timeout, stopping."
                        );

                        break;
                    }

                    continue;
                }

                System.out.println(
                        "[Worker] " + current.getName() +
                                " executes " + task
                );

                task.run();
            }

        } catch (InterruptedException e) {

            System.out.println(
                    "[Worker] " + current.getName() + " interrupted."
            );

            Thread.currentThread().interrupt();
        }

        System.out.println(
                "[Worker] " + current.getName() + " terminated."
        );

        pool.workerTerminated();
    }
}