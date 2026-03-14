package pool.rejection;

public interface RejectionPolicy {

    void reject(Runnable task);
}