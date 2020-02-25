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

    /**
     * 运行的2种模式：
     * 1.循环模式，创建固定的连接数，在每个连接上重复的产生不同的request和收到不同的response
     * 2.非循环模式，不断创建新的连接，每个连接上只产生一个request，收到一个对应的response
     */
    public static boolean isCyclic = true;

    private static final ArrayBlockingQueue ARRAY_QUEUE = new ArrayBlockingQueue(1000);
    private static final ExecutorService EXECUTOR_SERVICE = new ThreadPoolExecutor(20, 20, 5, TimeUnit.SECONDS, ARRAY_QUEUE);

    private void initSockets(){


        for (int i = 0; i < 20; i++) {

            try {
                Socket socket = new Socket("127.0.0.1", 3306); // 这就是bio同步阻塞
                SocketPool.add(socket);
            } catch (Exception ex) {
                logger.error("Exception occurs", ex);
            }

        }
    }

    private void finish(){

        logger.error(String.format("The total taskcount is:%d", TASK_COUNT.get()));
        //异步写日志的时候有可能没来得及输出进程就退出了
        System.out.println(String.format("The total taskcount is:%d", TASK_COUNT.get()));

        System.exit(1);

    }

     class TimeChecker implements Runnable {

        @Override
        public void run() {

            while (true) {
                //运行3分钟
                if (System.currentTimeMillis() - startTime >= 3 * 60 * 1000) {

                    finish();
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

        if(args.length == 1){
            isCyclic = Boolean.valueOf(args[0]);
        }

        BioClient bioClient = new BioClient();
        TimeChecker timeChecker =  bioClient. new TimeChecker();
        new Thread(timeChecker).start();

        if(isCyclic){
            bioClient.initSockets();
            try {
                for (int i = 0; i < 20; i++) {
                    ClientHandler handler = new ClientHandler(SocketPool.get(),isCyclic);// 创建一个任务
//                    handler.run();
                    EXECUTOR_SERVICE.execute(handler);// 任务交给线程池
                }
            } catch (Exception ex) {
                logger.error("Exception occurs", ex);
            }

        }else{

            while(true){
                Socket socket = new Socket("127.0.0.1", 3306);
                ClientHandler handler = new ClientHandler(socket,isCyclic);// 创建一个任务

                EXECUTOR_SERVICE.execute(handler);// 任务交给线程池
            }

        }

    }
}
