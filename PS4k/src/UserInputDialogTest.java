import com.jidesoft.plaf.LookAndFeelFactory;
import com.vitco.util.dialog.UserInputDialog;
import com.vitco.util.dialog.UserInputDialogListener;
import com.vitco.util.dialog.components.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;

public class UserInputDialogTest {

    public static void main(String[] args) {

        // the JIDE license
        com.jidesoft.utils.Lm.verifyLicense("Pixelated Games", "PS4K", "__JIDE_PASSWORD__");

        LookAndFeelFactory.installDefaultLookAndFeel();

        final JFrame frame = new JFrame("FrameDemo");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new JLabel("test"), BorderLayout.CENTER);
        frame.pack();
        frame.setExtendedState(frame.getExtendedState()|JFrame.MAXIMIZED_BOTH);
        frame.setVisible(true);

        // -------------------

        final UserInputDialog dialog = new UserInputDialog(frame, "Render Video", JOptionPane.CANCEL_OPTION);

        // add submit buttons
        dialog.addButton("Export", JOptionPane.OK_OPTION);
        dialog.addButton("Cancel", JOptionPane.CANCEL_OPTION);

        // add file select
        FieldSet location = new FieldSet("location", "Location");
        location.addComponent(new FileSelectModule("file", new File("exported"), frame));
        TextInputModule depthMapFileName = new TextInputModule("depth_map", "Depth Render File", new File("depthRender"), false);
        depthMapFileName.setEnabledLookup("export_type=image_renderer&image_renderer.render_depth=true");
        depthMapFileName.setVisibleLookup("export_type=image_renderer");
        location.addComponent(depthMapFileName);
        dialog.addFieldSet(location);

        // ---------------

        // set up Collada format
        FieldSet collada = new FieldSet("collada", "Collada");
        collada.addComponent(new LabelModule("Select Export Options:"));
        collada.addComponent(new ComboBoxModule("type", new String[][] {
                new String[] {"poly2tri", "Optimal (Poly2Tri)"},
                new String[] {"minimal", "Low Poly (Rectangular)"},
                new String[] {"legacy", "Legacy (Unoptimized)"}
        }, 0));
        // add information for "poly2tri"
        LabelModule poly2triInfo = new LabelModule("Info: This is the preferred exporter. The mesh is highly optimized and no rendering artifacts can appear.");
        poly2triInfo.setVisibleLookup("collada.type=poly2tri");
        collada.addComponent(poly2triInfo);
        // add information for "optimalGreedy"
        LabelModule minimalInfo = new LabelModule("Info: This exporter results in a slightly lower triangle count, however rendering artifacts can appear (T-Junction problems).");
        minimalInfo.setVisibleLookup("collada.type=minimal");
        collada.addComponent(minimalInfo);
        // add information for "legacy"
        LabelModule legacyInfo = new LabelModule("Info: Unoptimized legacy exporter. Useful if you want to process the mesh further. Suitable for 3D printing (uses vertex coloring).");
        legacyInfo.setVisibleLookup("collada.type=legacy");
        collada.addComponent(legacyInfo);

        // add options
        CheckBoxModule removeEnclosed = new CheckBoxModule("remove_holes", "Fill in enclosed holes", true);
        removeEnclosed.setInvisibleLookup("collada.type=legacy");
        collada.addComponent(removeEnclosed);

        // add options
        CheckBoxModule layersAsObjects = new CheckBoxModule("layers_as_objects", "Create a new object for every layer", false);
        layersAsObjects.setInvisibleLookup("collada.type=legacy");
        collada.addComponent(layersAsObjects);

        // use vertex colors
        CheckBoxModule useVertexColors = new CheckBoxModule("use_vertex_coloring", "Use vertex coloring (higher triangle count)", false);
        useVertexColors.setInvisibleLookup("collada.type=legacy");
        collada.addComponent(useVertexColors);

        // ---------------

        // add "render" export
        FieldSet imageRenderer = new FieldSet("image_renderer", "Render View");
        imageRenderer.addComponent(new CheckBoxModule("render_depth", "Render Depth Image", true));

        // ---------------

        // add all formats
        dialog.addComboBox("export_type", new FieldSet[] {collada, imageRenderer}, 0);

        // ---------------

        final ArrayList<String[]> serialization = new ArrayList<String[]>();

        // listen to events
        dialog.setListener(new UserInputDialogListener() {
            @Override
            public boolean onClose(int resultFlag) {
                if (resultFlag == JOptionPane.OK_OPTION) {
                    serialization.clear();
                    serialization.addAll(dialog.getSerialization());
                    for (String[] pair :serialization) {
                        System.out.println(pair[0] + "=" + pair[1]);
                    }
                    System.out.println(dialog.getValue("location.file"));
//                    System.out.println(dialog.getValue("location"));
//                    System.out.println(dialog.getValue("adobe_media_encoder.export_dae"));
//                    System.out.println(dialog.getValue("divx_encoder.export_img"));
//                    System.out.println(dialog.getValue("divx_encoder.export_img2"));
//                    System.out.println(dialog.getValue("divx_encoder.enable_overwrite"));
//                    System.out.println(dialog.getValue("encoder_type"));
                }
                return true;
            }
        });

        // -----------------

        frame.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                dialog.loadSerialization(serialization);
                dialog.setVisible(true);
            }
        });
    }

}