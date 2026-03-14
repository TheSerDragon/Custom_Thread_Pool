package pool.rejection;

public class CallerRunsPolicy implements RejectionPolicy {

    @Override
    public void reject(Runnable task) {

        System.out.println(
                "[Rejected] Task " + task +
                        " was rejected due to overload!"
        );

        task.run();
    }
}