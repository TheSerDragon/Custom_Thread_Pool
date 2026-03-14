package pool.balancing;

import pool.queue.TaskQueue;

public class LeastLoadedBalancer implements TaskBalancer {

    @Override
    public TaskQueue selectQueue(TaskQueue[] queues, int workerCount) {

        TaskQueue best = queues[0];

        for (int i = 1; i < workerCount; i++) {

            if (queues[i].size() < best.size()) {
                best = queues[i];
            }

        }

        return best;
    }
}