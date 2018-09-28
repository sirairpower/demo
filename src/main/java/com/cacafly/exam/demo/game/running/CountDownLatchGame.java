package com.cacafly.exam.demo.game.running;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CountDownLatchGame {
    private static CountDownLatch countDown = new CountDownLatch(4);

    public static void main(String[] args) {

        ExecutorService exe = Executors.newFixedThreadPool(3);

        System.out.println("Ready!!!!");

        for(int i=0;i<3;i++) {
            exe.execute(new Athlete(i+1, countDown));
            countDown.countDown();
        }


        //命令：跑...
        try {
            Thread.sleep(500L);//给一点时间,运动员动作准备就绪...
            System.out.println("Go!");
            countDown.countDown();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //执行完毕
        exe.shutdown();
    }
}
