package com.vitco.layout.bars;

import com.jidesoft.action.CommandBar;
import com.jidesoft.action.CommandMenuBar;
import com.vitco.engine.data.Data;
import com.vitco.util.action.types.StateActionPrototype;
import com.vitco.util.lang.LangSelectorInterface;
import org.springframework.beans.factory.annotation.Autowired;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;

/**
 * the main menu, uses menu generator to load content from file
 */
public class MainMenuLinkage extends BarLinkagePrototype {

    // var & setter (can not be interface!!)
    protected Data data;
    @Autowired
    public void setData(Data data) {
        this.data = data;
    }

    // var & setter
    protected LangSelectorInterface langSelector;
    @Autowired
    public void setLangSelector(LangSelectorInterface langSelector) {
        this.langSelector = langSelector;
    }

    // filter to only allow vsd files
    private static final class VSDFilter extends FileFilter
    {
        public boolean accept(File f)
        {
            return f.isDirectory() || f.getName().endsWith(".vsd");
        }

        public String getDescription()
        {
            return "PS4k File (*.vsd)";
        }
    }

    @Override
    public CommandBar buildBar(String key, final Frame frame) {
        final CommandMenuBar bar = new CommandMenuBar(key);

        // build the menu
        menuGenerator.buildMenuFromXML(bar, "com/vitco/layout/bars/main_menu.xml");

        // util for save/load/new file
        final String[] save_location = new String[] {null}; // the location of active file (or null if none active)
        final JFileChooser fc = new JFileChooser();
        fc.addChoosableFileFilter(new VSDFilter());
        fc.setFileFilter(new VSDFilter());

        // save file
        actionManager.registerAction("save_file_action", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int returnVal = fc.showSaveDialog(frame);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    // make sure filename ends with *.vsd
                    String dir = fc.getSelectedFile().getPath();
                    if(!dir.toLowerCase().endsWith(".vsd")) {
                        dir += ".vsd";
                    }
                    File saveTo = new File(dir);
                    // query if file already exists
                    if (!saveTo.exists() ||
                            JOptionPane.showConfirmDialog(frame,
                                    dir + " " + langSelector.getString("replace_file_query"),
                                    langSelector.getString("replace_file_query_title"),
                                    JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                        // save file and remember it
                        data.saveToFile(saveTo);
                        save_location[0] = dir;
                    }
                }
            }
        });

        // load file
        actionManager.registerAction("load_file_action", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!data.hasChanged() || JOptionPane.showConfirmDialog(frame,
                        langSelector.getString("unsaved_changes_on_open"),
                        langSelector.getString("unsaved_changes_on_open_title"),
                        JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                    int returnVal = fc.showOpenDialog(frame);
                    if (returnVal == JFileChooser.APPROVE_OPTION) {
                        data.loadFromFile(fc.getSelectedFile());
                        save_location[0] = fc.getSelectedFile().getPath(); // remember load location
                    }
                }
            }
        });

        // quick save
        actionManager.registerAction("quick_save_file_action", new StateActionPrototype() {
            @Override
            public void action(ActionEvent actionEvent) {
                if (getStatus()) { // make sure we can save
                    File file = new File(save_location[0]);
                    data.saveToFile(file);
                }
            }

            @Override
            public boolean getStatus() {
                return save_location[0] != null;
            }
        });

        // new file
        actionManager.registerAction("new_file_action", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (data.hasChanged()) {
                    // option to save changes / erase changes / cancel
                    switch (JOptionPane.showConfirmDialog(frame, langSelector.getString("save_current_changed_query"))) {
                        case JOptionPane.YES_OPTION: // save changes
                            if (save_location[0] != null) { // we already know where to save (ok)
                                File file = new File(save_location[0]);
                                data.saveToFile(file);
                                save_location[0] = null;
                                data.freshStart();
                            } else { // we dont know where
                                int returnVal = fc.showSaveDialog(frame);
                                if (returnVal == JFileChooser.APPROVE_OPTION) { // location selected
                                    // query if the file already exists
                                    if (!fc.getSelectedFile().exists() ||
                                            JOptionPane.showConfirmDialog(frame,
                                                    fc.getSelectedFile().getName() + " " + langSelector.getString("replace_file_query"),
                                                    langSelector.getString("replace_file_query_title"),
                                                    JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                                        data.saveToFile(fc.getSelectedFile());
                                        save_location[0] = null;
                                        data.freshStart();
                                    }
                                }
                            }
                            break;
                        case JOptionPane.NO_OPTION: // don't save option
                            data.freshStart();
                            save_location[0] = null;
                            break;
                        case JOptionPane.CANCEL_OPTION: // cancel = do nothing
                            // cancel
                            break;
                    }
                } else {
                    data.freshStart();
                    save_location[0] = null;
                }
            }
        });

        return bar;
    }
}
