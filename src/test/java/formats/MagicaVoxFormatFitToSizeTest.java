package formats;

import com.vitco.app.core.data.Data;
import com.vitco.app.export.AbstractExporter;
import com.vitco.app.export.MagicaVoxelExporter;
import com.vitco.app.importer.AbstractImporter;
import com.vitco.app.importer.VoxImporter;
import com.vitco.app.util.components.progressbar.ProgressDialog;

import java.io.File;
import java.io.IOException;


public class MagicaVoxFormatFitToSizeTest extends AbstractFormatTest {

    public MagicaVoxFormatFitToSizeTest() {
        super("magica_vox" + File.separator + "fit_to_size");
    }

    @Override
    AbstractImporter initImporter(File file) throws IOException {
        return new VoxImporter(file, "Importer");
    }

    @Override
    AbstractExporter initExporter(File file, Data data) throws IOException {
        return new MagicaVoxelExporter(file, data, new ProgressDialog(null), null, true);
    }

    @Override
    boolean shiftToCenter() {
        return false;
    }
}
