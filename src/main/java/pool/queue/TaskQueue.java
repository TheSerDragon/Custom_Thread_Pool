package pool.queue;

import java.util.concurrent.ArrayBlockingQueue;

public class TaskQueue extends ArrayBlockingQueue<Runnable> {

    private final int id;

    public TaskQueue(int capacity, int id) {
        super(capacity);
        this.id = id;
    }

    public int getId() {
        return id;
    }

    @Override
    public boolean offer(Runnable task) {

        boolean result = super.offer(task);

        if (result) {

            System.out.println(
                    "[Pool] Task accepted into queue #" + id + ": " + task
            );

        }

        return result;
    }
}