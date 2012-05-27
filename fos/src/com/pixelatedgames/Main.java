package com.pixelatedgames;

import com.pixelatedgames.fos.FantasyServer;

/**
 * User: J
 * Date: 3/4/12
 * Time: 10:29 AM
 */
public class Main {
    private static final int port = 443;
    private static final FantasyServer fs = new FantasyServer(port);

    public static void main(String[] args) {        
        fs.run();
    }
}
