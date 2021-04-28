package com.holyes.ccssend5.utils;

/**
 * @ClassName: MyThread
 * @Description: java类作用描述
 * @Author: lijin
 * @Date: 2021/3/10 10:01
 */
public class MyThread extends Thread {

        private final Object lock = new Object();
        private boolean pause = false;

        /**
         * 调用这个方法实现暂停线程
         */
        public void pauseThread() {
            pause = true;
        }

        /**
         * 调用这个方法实现恢复线程的运行
         */
        public void resumeThread() {
            pause = false;
            synchronized (lock) {
                lock.notifyAll();
            }
        }

        /**
         * 注意：这个方法只能在run方法里调用，不然会阻塞主线程，导致页面无响应
         */
        void onPause() {
            synchronized (lock) {
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void run() {
            super.run();
            try {
                int index = 0;
                while (true) {
                    // 让线程处于暂停等待状态
                    while (pause) {
                        onPause();
                    }
                    try {
                        System.out.println(index);
                        Thread.sleep(50);
                        ++index;
                    } catch (InterruptedException e) {
                        //捕获到异常之后，执行break跳出循环
                        break;
                    }
                }
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
    }

