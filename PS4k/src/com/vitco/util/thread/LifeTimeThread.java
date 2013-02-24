package com.vitco.util.thread;


public abstract class LifeTimeThread extends Thread {
    // run the loop till interrupted
    public final void run() {
        try {
            //noinspection InfiniteLoopStatement
            while (true) {
                loop();
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    // to interrupt the thread
    public final void stopThread() {
        interrupt();
    }

    // loop to be defines
    @SuppressWarnings("RedundantThrows")
    public abstract void loop() throws InterruptedException;
}
