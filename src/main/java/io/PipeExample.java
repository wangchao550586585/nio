package io;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

/**
 * Java IO中的管道为运行在同一个JVM中的两个线程提供了通信的能力。所以管道也可以作为数据源以及目标媒介
 *
 * DataInputStream 读取的数据由大于一个字节的Java原语（如int，long，float，double等）组成
 * SequenceInputStream 将两个或者多个输入流当成一个输入流依次读取
 */
public class PipeExample {
    public static void main(String[] args) throws IOException {
        final PipedOutputStream pipedOutputStream = new PipedOutputStream();
        final PipedInputStream pipedInputStream = new PipedInputStream(pipedOutputStream);
        //pipedInputStream.connect(pipedOutputStream); 也可以连接
        Thread thread1 = new Thread(new Runnable() {
            public void run() {
                try {
                    pipedOutputStream.write("hello".getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        Thread thread = new Thread(new Runnable() {
            public void run() {
                try {
                    int read;
                    while ((read = pipedInputStream.read()) != -1) {
                        System.out.println((char) read);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
        thread1.start();


    }
}
