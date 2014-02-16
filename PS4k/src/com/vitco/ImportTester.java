package com.vitco;

import com.vitco.importer.KvxImporter;

import java.io.File;
import java.io.IOException;

/**
 * Testing for the importer
 */
public class ImportTester {
    public static void main(String[] args) throws IOException {
        KvxImporter kvx = new KvxImporter(new File("pawn.kvx"));
        if (kvx.hasLoaded()) {
            System.out.println("Loading finished.");
        }

    }
}
