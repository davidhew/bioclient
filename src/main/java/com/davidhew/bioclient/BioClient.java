package com.davidhew.bioclient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by shouru on 2018/9/16.
 */
public class BioClient {

    private static final Logger logger = LoggerFactory.getLogger(BioClient.class.getName());

    public static final AtomicLong TASK_COUNT = new AtomicLong(0);

    private static long startTime = System.currentTimeMillis();

    private static boolean run = true;

    private static final ArrayBlockingQueue ARRAY_QUEUE = new ArrayBlockingQueue(100);
    private static final ExecutorService EXECUTOR_SERVICE = new ThreadPoolExecutor(20, 20, 5, TimeUnit.SECONDS, ARRAY_QUEUE);

    static {

        if (logger.isInfoEnabled()) {

            logger.info("initiate socket pool");
        }

        for (int i = 0; i < 20; i++) {

            try {
                Socket socket = new Socket("127.0.0.1", 3306); // 这就是bio同步阻塞
                SocketPool.add(socket);
            } catch (Exception ex) {
                logger.error("Exception occurs", ex);
            }

        }
    }

    static class TimeChecker implements Runnable {

        @Override
        public void run() {

            while (true) {
                //运行3分钟
                if (System.currentTimeMillis() - startTime >= 3 * 60 * 1000) {

                    run = false;
                } else {
                    try {
                        Thread.sleep(1000);
                    } catch (Exception ex) {
                        logger.error("Exception occurs", ex);
                    }
                }
            }
        }
    }

    public static void main(String[] args) throws Exception {

        new Thread(new TimeChecker()).start();
        while (true) {
            if (!run) {

                logger.error(String.format("The total taskcount is:%d", TASK_COUNT.get()));
                break;
            }
            try {
                Socket socket = SocketPool.get();
                ClientHandler handler = new ClientHandler(socket);// 创建一个任务
                EXECUTOR_SERVICE.execute(handler);// 任务交给线程池
            } catch (Exception ex) {
                logger.error("Exception occurs", ex);
            }
        }
    }
}
