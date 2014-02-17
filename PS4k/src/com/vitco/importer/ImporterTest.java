package com.vitco.importer;

import com.vitco.util.file.FileTools;
import org.junit.Test;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Extensive test for voxel data.
 */

public class ImporterTest {

    // load all files in directory
    private void doTest(String directory, String ext, Class importerClass) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        File[] files = FileTools.findFiles(directory, ext);

        @SuppressWarnings("unchecked")
        Constructor ctor = importerClass.getConstructor(File.class, String.class);
        for (File file : files) {
            System.out.print("Checking " + file.getName() + "... ");
            AbstractImporter importer = (AbstractImporter) ctor.newInstance(file, "Import");
            assert importer.hasLoaded();
            for (AbstractImporter.Layer layer : importer.getVoxel()) {
                assert !layer.isEmpty();
            }
            System.out.println("ok.");
        }
    }

    @Test
    public void testAllImporter() throws Exception {
        doTest("C:\\Users\\flux\\Desktop\\binvox", "binvox", BinVoxImporter.class);
        doTest("C:\\Users\\flux\\Desktop\\kv6", "kv6", Kv6Importer.class);
        doTest("C:\\Users\\flux\\Desktop\\kvx", "kvx", KvxImporter.class);
        doTest("C:\\Users\\flux\\Desktop\\qb", "qb", QbImporter.class);
    }

    @Test
    public void testNewImporter() throws Exception {
//        AbstractImporter kvx = new Kv6Importer(new File("cac.kv6"), "Import");
//        if (kvx.hasLoaded()) {
//            System.out.println("Loading finished.");
//        }
    }

}
