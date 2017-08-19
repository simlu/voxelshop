package formats;

import com.vitco.app.core.data.Data;
import com.vitco.app.export.AbstractExporter;
import com.vitco.app.export.PnxExporter;
import com.vitco.app.importer.AbstractImporter;
import com.vitco.app.importer.PnxImporter;
import com.vitco.app.util.components.progressbar.ProgressDialog;

import java.io.File;
import java.io.IOException;


public class PNXFormatTest extends AbstractFormatTest {

    // IMPORTANT: png compression is not deterministic.
    // Hence test could randomly fail, but seems to be fine with provided files.

    public PNXFormatTest() {
        super("pnx");
    }

    @Override
    AbstractImporter initImporter(File file) throws IOException {
        return new PnxImporter(file, "Importer");
    }

    @Override
    AbstractExporter initExporter(File file, Data data) throws IOException {
        return new PnxExporter(file, data, new ProgressDialog(null), null);
    }

    @Override
    boolean shiftToCenter() {
        return false;
    }
}
