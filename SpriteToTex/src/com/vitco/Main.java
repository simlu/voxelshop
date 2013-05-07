package com.vitco;

import com.vitco.logic.SpriteManager;

import java.io.File;

/**
 * Main class that gets executed.
 */
public class Main {
    public static void main(String[] args) {

        String dir = "C:\\Users\\VM Win 7\\Dropbox\\Artwork\\Sprites\\~tmp\\";
        if (new File("/var").exists()) {
            // change dir
            dir = "/var/www/";
        }

        SpriteManager spriteManager = new SpriteManager();
        spriteManager.generateFiles(dir);
    }
}
