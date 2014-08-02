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
        doTest("C:\\Users\\flux\\Desktop\\vxl (2)", "vxl", CCVxlImporter.class);
        doTest("C:\\Users\\flux\\Desktop\\rawvox", "rawvox", RawVoxImporter.class);
        doTest("C:\\Users\\flux\\Desktop\\vox", "vox", VoxImporter.class);
        doTest("C:\\Users\\flux\\Desktop\\binvox", "binvox", BinVoxImporter.class);
        doTest("C:\\Users\\flux\\Desktop\\kvx", "kvx", KvxImporter.class);
        doTest("C:\\Users\\flux\\Desktop\\qb", "qb", QbImporter.class);
        doTest("C:\\Users\\flux\\Desktop\\kv6", "kv6", Kv6Importer.class);
    }

    @Test
    public void testNewImporter() throws Exception {
        AbstractImporter importer = new CCVxlImporter(new File("C:\\Users\\flux\\Desktop\\vxl (2)\\abrams.vxl"), "Import");
        if (importer.hasLoaded()) {
            System.out.println("Loading finished.");
        }
    }

}
