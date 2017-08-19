package formats;

import com.vitco.app.core.data.Data;
import com.vitco.app.export.AbstractExporter;
import com.vitco.app.export.VoxVoxLapExporter;
import com.vitco.app.importer.AbstractImporter;
import com.vitco.app.importer.VoxImporter;
import com.vitco.app.util.components.progressbar.ProgressDialog;

import java.io.File;
import java.io.IOException;


public class VoxLapEngineFormatTest extends AbstractFormatTest {

    public VoxLapEngineFormatTest() {
        super("vox_lap_engine");
    }

    @Override
    AbstractImporter initImporter(File file) throws IOException {
        return new VoxImporter(file, "Importer");
    }

    @Override
    AbstractExporter initExporter(File file, Data data) throws IOException {
        return new VoxVoxLapExporter(file, data, new ProgressDialog(null), null);
    }

    @Override
    boolean shiftToCenter() {
        return false;
    }
}
