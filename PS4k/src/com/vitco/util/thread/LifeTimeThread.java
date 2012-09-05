package com.vitco.util.thread;


public abstract class LifeTimeThread extends Thread {
    private boolean stopFlag = false;

    // run the loop till interrupted
    public final void run() {
        try {
            //noinspection InfiniteLoopStatement
            while (true) {
                loop();

                if (stopFlag) {
                    throw new InterruptedException();
                }
            }
        } catch (InterruptedException ignored) {}
    }

    // to interrupt the thread
    public final void stopThread() {
        stopFlag = true;
    }

    // loop to be defines
    @SuppressWarnings("RedundantThrows")
    public abstract void loop() throws InterruptedException;
}
