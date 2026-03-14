package pool.demo;

import pool.CustomThreadPool;

import java.util.concurrent.TimeUnit;

public class Main {

    public static void main(String[] args) throws Exception {

        CustomThreadPool pool =
                new CustomThreadPool(
                        2,
                        4,
                        5,
                        1,
                        5,
                        TimeUnit.SECONDS
                );

        for (int i = 0; i < 50; i++) {

            pool.execute(new DemoTask(i));
        }

        Thread.sleep(15000);

        pool.shutdown();
    }
}