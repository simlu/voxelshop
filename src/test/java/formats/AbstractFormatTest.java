package formats;

import com.vitco.app.core.data.Data;
import com.vitco.app.export.AbstractExporter;
import com.vitco.app.importer.AbstractImporter;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public abstract class AbstractFormatTest {

    private static final String directory = "src/test/resources/formats/";
    private final String folder;

    AbstractFormatTest(String folder) {
        this.folder = folder;
    }

    @Test
    public void runTests() throws Exception {
        for (File file : getFiles()) {
            execute(file);
        }
    };

    private File[] getFiles() {
        return new File(directory + this.folder).listFiles();
    }

    abstract AbstractImporter initImporter(File file) throws IOException;
    abstract AbstractExporter initExporter(File file, Data data) throws IOException;
    abstract boolean shiftToCenter();

    // do the actual test for file
    private void execute(File file) throws IOException {
        Data data = new Data();
        data.deleteLayer(data.getLayers()[0]);
        AbstractImporter importer = initImporter(file);

        assertTrue(file.getName(), importer.hasLoaded());

        importer.loadInto(data, shiftToCenter());

        File temp = File.createTempFile("temp-file-name", ".tmp");
        AbstractExporter exporter = initExporter(temp, data);
        exporter.writeData();

        byte[] f1 = Files.readAllBytes(file.toPath());
        byte[] f2 = Files.readAllBytes(temp.toPath());
        assertEquals(file.getName(), f1.length, f2.length);
        assertTrue(file.getName(), Arrays.equals(f1, f2));
    }
}
