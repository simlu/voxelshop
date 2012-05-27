package com.pixelatedgames.fos;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * User: J
 * Date: 3/19/12
 * Time: 2:24 PM
 */
public class FantasyServerUpdateLoop implements Runnable{
    private static final Logger logger = LoggerFactory.getLogger(FantasyServerUpdateLoop.class);

    private final FantasyServer _fantasyServer;

    private long current;
    private long accumulator;
    private long step;
    private long maxAccumulation;

    public FantasyServerUpdateLoop(FantasyServer fantasyServer, long framerate) {
        _fantasyServer = fantasyServer;
        step = 1000000000 / framerate;      // we step at the framerate
        maxAccumulation = step * 2;         // max accumulation is double the framerate
        current = System.nanoTime();        // init the current
        accumulator = 0;                    // init the accumulator
    }

    @Override
    public void run() {
        long mark =  System.nanoTime();             // current time
        long elapsed = mark - current;              // elapsed since last
        current = mark;                             // set the new current

        accumulator += elapsed;                     // accumulate
        if(accumulator > maxAccumulation) {         // cap at max accumulation
            accumulator = maxAccumulation;
        }

        while(accumulator >= step) {                 // step until we're out of accumulated time
            //logger.info("{} {}", accumulator, step);

            // step here
            _fantasyServer.periodicProcess(step);

            accumulator -= step;                    // count down
        }
    }
}
