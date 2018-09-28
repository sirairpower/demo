package com.cacafly.exam.demo.game.running;

import java.util.concurrent.CountDownLatch;

public class Athlete implements Runnable {
    private final int id;
    private CountDownLatch countDown;

    public Athlete(int id, CountDownLatch countDown) {
        this.id = id;
        this.countDown = countDown;
    }

//    public boolean equals(Object o) {
//        if (!(o instanceof Athlete))
//            return false;
//        Athlete athlete = (Athlete) o;
//        return id == athlete.id;
//    }

    public String toString() {
        return "Athlete<" + id + ">";
    }

//    public int hashCode() {
//        return new Integer(id).hashCode();
//    }

    public void run() {
        try {
            System.out.println(this + " ready");//报告准备就绪
            countDown.await();//阻塞
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println(this + " go~~~"); //开始跑...
    }
}
