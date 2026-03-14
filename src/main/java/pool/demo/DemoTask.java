package pool.demo;

public class DemoTask implements Runnable {

    private final int id;

    public DemoTask(int id) {
        this.id = id;
    }

    @Override
    public void run() {

        System.out.println(
                "[Task] Started " + id +
                        " in " + Thread.currentThread().getName()
        );

        try {
            Thread.sleep(2000);
        } catch (InterruptedException ignored) {}

        System.out.println("[Task] Finished " + id);
    }

    public String toString() {
        return "DemoTask-" + id;
    }
}