package com.davidhew.bioclient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Random;

/**
 * Created by shouru on 2018/9/16.
 */
public class ClientHandler implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(ClientHandler.class.getName());

    private Socket socket;
    private InputStream is;
    private OutputStream os;
    private StringBuilder strB = new StringBuilder(1024);

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            is = socket.getInputStream();
            os = socket.getOutputStream();
            byte[] byteBuffer = new byte[1024];

            Command command = null;
            int i = new Random().nextInt(1000);
            if (i % 2 == 0) {
                command = Command.ComputePrime;
            } else {
                command = Command.ComputeSum;
            }

            if (i < 3) {
                i = 3;
            }
            String request = Thread.currentThread().getName() + ";" + command.name() + ";" + i + "\n";
            os.write(request.getBytes("UTF-8"));
            os.flush();
            if(logger.isInfoEnabled()){
                logger.info(request);
            }
            StringBuilder strB = new StringBuilder(1024);

            while (!strB.toString().endsWith("\n")) {
                int result = is.read(byteBuffer);

                if (result == -1) {
                    break;
                }
                String str = new String(byteBuffer, 0,result,"utf-8");
                strB.append(str);

            }

            if(logger.isInfoEnabled()){
                logger.info("The response is: "+strB.toString());
            }

            BioClient.TASK_COUNT.getAndIncrement();

        } catch (Exception e) {
            logger.error("Exception occurs,", e);
        }
        finally{

            //return socket to pool
            SocketPool.add(socket);
        }
    }


}
