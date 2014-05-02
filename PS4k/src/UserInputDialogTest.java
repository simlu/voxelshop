import com.jidesoft.plaf.LookAndFeelFactory;
import com.vitco.util.dialog.FieldSet;
import com.vitco.util.dialog.UserInputDialog;
import com.vitco.util.dialog.UserInputDialogListener;
import com.vitco.util.dialog.components.CheckBoxModule;
import com.vitco.util.dialog.components.FileSelectModule;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

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

        final UserInputDialog userInputDialog = new UserInputDialog(frame, "Render Video", JOptionPane.CANCEL_OPTION);
        // add submit buttons
        userInputDialog.addButton("Render", JOptionPane.OK_OPTION);
        userInputDialog.addButton("Cancel", JOptionPane.CANCEL_OPTION);
        // create file select FieldSet
        FieldSet fieldSet = new FieldSet("location", "Location");
        fieldSet.addComponent(new FileSelectModule("file", new File("C:\\Users\\flux\\Dropbox\\"), frame));
        userInputDialog.addFieldSet(fieldSet);
        // create menu tree
        FieldSet[] menuItems = new FieldSet[] {
                new FieldSet("adobe_media_encoder", "Adobe Media Encoder"),
                new FieldSet("divx_encoder", "DivX Encoder")
        };
        menuItems[0].addComponent(new FileSelectModule("export_dae", new File("tmp.dae"), frame));
        menuItems[1].addComponent(new FileSelectModule("export_img", new File("tmp.png"), frame));
        menuItems[1].addComponent(new FileSelectModule("export_img2", new File("tmp.png"), frame));
        menuItems[1].addComponent(new CheckBoxModule("enable_overwrite", "Overwrite File"));
        userInputDialog.addDropDown("encoder_type", menuItems, 1);

        // listen to events
        userInputDialog.setListener(new UserInputDialogListener() {
            @Override
            public boolean onClose(int resultFlag) {
                if (resultFlag == JOptionPane.OK_OPTION) {
                    System.out.println(userInputDialog.getValue("location.file"));
                    System.out.println(userInputDialog.getValue("location"));
                    System.out.println(userInputDialog.getValue("adobe_media_encoder.export_dae"));
                    System.out.println(userInputDialog.getValue("divx_encoder.export_img"));
                    System.out.println(userInputDialog.getValue("divx_encoder.export_img2"));
                    System.out.println(userInputDialog.getValue("divx_encoder.enable_overwrite"));
                    System.out.println(userInputDialog.getValue("encoder_type"));
                }
                return true;
            }
        });

        // -----------------

        frame.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                userInputDialog.setVisible(true);
            }
        });
    }

}