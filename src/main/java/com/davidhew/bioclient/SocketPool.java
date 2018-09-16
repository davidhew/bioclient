package com.davidhew.bioclient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Created by shouru on 2018/9/16.
 */
public class SocketPool {

    private static final Logger logger = LoggerFactory.getLogger(SocketPool.class.getName());

    private static ArrayBlockingQueue<Socket> queue = new ArrayBlockingQueue(20);



    public static  void add(Socket sk){
        queue.add(sk);
    }

    public static Socket get(){
        Socket result = null;
        while(result == null) {
            try {
                result =  queue.take();
            } catch (InterruptedException ex) {
                logger.error(Thread.currentThread().getName()+" interrupted!");
            }
        }
        return result;
    }
}
