package imggame.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Async {
    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(4);

    public static void run(Runnable task) {
        EXECUTOR.submit(task);
    }
}
