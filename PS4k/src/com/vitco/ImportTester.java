package com.vitco;

import com.vitco.importer.AbstractImporter;
import com.vitco.importer.QbImporter;

import java.io.File;
import java.io.IOException;

/**
 * Testing for the importer
 */
public class ImportTester {
    public static void main(String[] args) throws IOException {
        AbstractImporter kvx = new QbImporter(new File("nerds.qb"), "nerds");
        if (kvx.hasLoaded()) {
            System.out.println("Loading finished.");
        }

    }
}
