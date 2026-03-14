package pool.balancing;

import pool.queue.TaskQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class RoundRobinBalancer implements TaskBalancer {

    private final AtomicInteger counter = new AtomicInteger();

    @Override
    public TaskQueue selectQueue(TaskQueue[] queues, int workerCount) {

        int index = Math.abs(counter.getAndIncrement() % workerCount);

        return queues[index];
    }
}