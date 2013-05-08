package com.vitco.util.thread;


public abstract class LifeTimeThread extends Thread {
    // run the loop till interrupted
    public final void run() {
        try {
            //noinspection InfiniteLoopStatement
            while (!stopped) {
                loop();
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    private boolean stopped = false;

    // to interrupt the thread
    public final void stopThread() {
        onBeforeStop();
        stopped = true;
        interrupt();
    }

    public void onBeforeStop() {}

    public final boolean wasStopped() {
        return stopped;
    }

    // loop to be defines
    @SuppressWarnings("RedundantThrows")
    public abstract void loop() throws InterruptedException;
}
