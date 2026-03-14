package pool.balancing;

import pool.queue.TaskQueue;

public interface TaskBalancer {

    TaskQueue selectQueue(TaskQueue[] queues, int workerCount);
}