package com.vitco.layout.content.console;

import com.jidesoft.action.CommandMenuBar;
import com.threed.jpct.Texture;
import com.threed.jpct.TextureManager;
import com.vitco.Main;
import com.vitco.core.data.Data;
import com.vitco.core.data.container.Voxel;
import com.vitco.export.generic.ExportWorld;
import com.vitco.layout.content.JCustomScrollPane;
import com.vitco.layout.content.ViewPrototype;
import com.vitco.layout.frames.FrameLinkagePrototype;
import com.vitco.low.hull.HullManagerExt;
import com.vitco.manager.action.types.StateActionPrototype;
import com.vitco.manager.async.AsyncAction;
import com.vitco.manager.async.AsyncActionManager;
import com.vitco.manager.thread.LifeTimeThread;
import com.vitco.manager.thread.ThreadManagerInterface;
import com.vitco.settings.VitcoSettings;
import com.vitco.util.misc.DateTools;
import org.springframework.beans.factory.annotation.Autowired;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.net.URLDecoder;
import java.util.*;

/**
 * Displays console content and buttons to user.
 */
public class ConsoleView extends ViewPrototype implements ConsoleViewInterface {

    // var & setter
    protected Data data;
    @Autowired(required=true)
    public final void setData(Data data) {
        this.data = data;
    }

    // var & setter
    protected AsyncActionManager asyncActionManager;
    @Autowired(required=true)
    public final void setAsyncActionManager(AsyncActionManager asyncActionManager) {
        this.asyncActionManager = asyncActionManager;
    }

    private ThreadManagerInterface threadManager;
    // set the action handler
    @Autowired
    public final void setThreadManager(ThreadManagerInterface threadManager) {
        this.threadManager = threadManager;
    }

    @Override
    public JComponent buildConsole(final FrameLinkagePrototype frame) {
        // panel that holds everything
        final JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        // the console
        final JTextArea textArea = new JTextArea();
        // set layout
        textArea.setForeground(VitcoSettings.DEFAULT_TEXT_COLOR);
        textArea.setBackground(VitcoSettings.DEFAULT_BG_COLOR);
        // load the previous console data
        final ArrayList<String> consoleData = console.getConsoleData();
        for (String line : consoleData) {
          textArea.append(line);
        }
        textArea.setEditable(false); // hide the caret
        // to be able to handle auto show, auto scroll and
        // tmp disable scroll for this textarea
        class ScrollPane extends JCustomScrollPane {
            public boolean autoScroll = true; // true iff auto scrolling is enabled
            public boolean autoShow = true; // true iff auto showing is enabled
            public boolean tempScrollStop = false; // true iff user scrolls (disable auto scroll!)
            public ScrollPane(JComponent component) {
                super(component);
            }
        }
        final ScrollPane scrollPane = new ScrollPane(textArea);
        scrollPane.setBorder(BorderFactory.createMatteBorder(1,0,1,1,VitcoSettings.DEFAULT_BORDER_COLOR));
        // load the stored preferences for this
        if (preferences.contains("console_auto_scroll_status")) {
            scrollPane.autoScroll = preferences.loadBoolean("console_auto_scroll_status");
        }
        if (preferences.contains("console_auto_show_status")) {
            scrollPane.autoShow = preferences.loadBoolean("console_auto_show_status");
        }
        // only scroll when the user is not scrolling
        scrollPane.getVerticalScrollBar().addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                scrollPane.tempScrollStop = true;
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                scrollPane.tempScrollStop = false;
            }
        });
        // handle console events
        console.addConsoleListener(new ConsoleListener() {
            @Override
            public void lineAdded(String line) {
                // show if necessary
                if (scrollPane.autoShow && (!frame.isVisible() || // not visible
                        (frame.isAutohide() && !frame.isAutohideShowing()))) { // visible but not showing (side)
                    frame.setVisible(true); // show the console whenever text is added
                    panel.repaint();
                }
                // add line
                textArea.append(line);
                // make sure there are not too many lines in the textarea
                while (textArea.getLineCount() - 1 > Console.LINE_BUFFER_COUNT) {
                    try {
                        // remove first line
                        Element root = textArea.getDocument().getDefaultRootElement();
                        Element first = root.getElement(0);
                        textArea.getDocument().remove(first.getStartOffset(), first.getEndOffset());
                    } catch (BadLocationException e) {
                        errorHandler.handle(e);
                    }
                }
                // scroll to bottom if wanted
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        if (!scrollPane.tempScrollStop && scrollPane.autoScroll) {
                            // when the text changes, scroll down
                            scrollPane.validate();
                            scrollPane.getVerticalScrollBar().setValue(scrollPane.getVerticalScrollBar().getMaximum());
                        }
                    }
                });
            }
        });

        final JTextField inputField = new JTextField();

        // produce error
        actionManager.registerAction("create_error_for_debug", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                errorHandler.handle(new Exception("Debug Exception"));
            }
        });

        // study the complexity of the currently visible polygon
        actionManager.registerAction("study_object_complexity", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                asyncActionManager.addAsyncAction(new AsyncAction() {
                    @Override
                    public void performAction() {
                        console.addLine("Computing \"Reduced Triangle Count\"...");
                        ExportWorld exportWorld = new ExportWorld(data.getVisibleLayerVoxel());
                        int[] countInfo = exportWorld.analyzeTriCount(ExportWorld.ALGORITHM_GREEDY);
                        console.addLine("Naive Greedy Meshing: " + countInfo[0] + " triangles (" + countInfo[1] + " before) in " + countInfo[2] + "ms");
                        countInfo = exportWorld.analyzeTriCount(ExportWorld.ALGORITHM_GREEDY_OPTIMAL);
                        console.addLine("Optimal Greedy Meshing: " + countInfo[0] + " triangles (" + countInfo[1] + " before) in " + countInfo[2] + "ms");
                        countInfo = exportWorld.analyzeTriCount(ExportWorld.ALGORITHM_MONO);
                        console.addLine("Monotone Meshing: " + countInfo[0] + " triangles (" + countInfo[1] + " before) in " + countInfo[2] + "ms");
                        countInfo = exportWorld.analyzeTriCount(ExportWorld.ALGORITHM_MONO_SAVE);
                        console.addLine("Monotone Meshing (Save in 2D): " + countInfo[0] + " triangles (" + countInfo[1] + " before) in " + countInfo[2] + "ms");
                        countInfo = exportWorld.analyzeTriCount(ExportWorld.ALGORITHM_POLY2TRI);
                        console.addLine("Poly2Tri Meshing (Without mesh fixing): " + countInfo[0] +
                                " triangles (" + countInfo[1] + " before) in " + countInfo[2] + "ms");
                    }
                });
            }
        });

        // check for deadlock
        actionManager.registerAction("check_for_deadlock_toggle", new AbstractAction() {
            private boolean active = false;
            private LifeTimeThread thread;

            @Override
            public void actionPerformed(ActionEvent e) {
                active = !active;
                if (active) {
                    thread = new LifeTimeThread() {
                        @Override
                        public void loop() throws InterruptedException {
                            // -- check for deadlocks
                            ThreadMXBean bean = ManagementFactory.getThreadMXBean();
                            // Returns null if no threads are deadlocked.
                            long[] threadIds = bean.findDeadlockedThreads();

                            if (threadIds != null) {
                                // retrieve up to 100 lines of stack trace
                                ThreadInfo[] infos = bean.getThreadInfo(threadIds, 100);

                                // print the deadlock information to file
                                String path = getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
                                try {
                                    String appJarLocation = URLDecoder.decode(path, "UTF-8");
                                    File appJar = new File(appJarLocation);
                                    String absolutePath = appJar.getAbsolutePath();
                                    String filePath = absolutePath.
                                            substring(0, absolutePath.lastIndexOf(File.separator) + 1);
                                    PrintWriter out = new PrintWriter(new BufferedWriter(
                                            new OutputStreamWriter(new FileOutputStream(filePath + "errorlog.txt", true),"UTF-8")));
                                    out.println("===================");
                                    out.println(DateTools.now("yyyy-MM-dd HH-mm-ss"));
                                    out.println("-------------------");
                                    out.println("Deadlock");
                                    if (Main.isDebugMode()) {
                                        System.err.println("Deadlock");
                                    }
                                    for (ThreadInfo info : infos) {
                                        out.println(info.toString());
                                        // Log or store stack trace information.
                                        for (StackTraceElement ele : info.getStackTrace()) {
                                            out.println(":: " + ele.toString());
                                        }
                                        out.println("-------------------");
                                        if (Main.isDebugMode()) {
                                            System.err.println(info.toString());
                                            // Log or store stack trace information.
                                            for (StackTraceElement ele : info.getStackTrace()) {
                                                System.err.println(":: " + ele.toString());
                                            }
                                            System.err.println("======");
                                        }
                                    }
                                    out.println();
                                    out.close();
                                } catch (UnsupportedEncodingException ex) {
                                    // If this fails, the program is not reporting.
                                    if (Main.isDebugMode()) {
                                        ex.printStackTrace();
                                    }
                                } catch (IOException ex) {
                                    // If this fails, the program is not reporting.
                                    if (Main.isDebugMode()) {
                                        ex.printStackTrace();
                                    }
                                }
                                // prevent further printing
                                thread.stopThread();
                            }
                            Thread.sleep(5000);
                        }
                    };
                    threadManager.manage(thread);
                    console.addLine("Deadlock checking is activated.");
                } else {
                    threadManager.remove(thread);
                    console.addLine("Deadlock checking is deactivated.");
                }
            }
        });

        // start/stop test mode (rapid adding/removing of voxel)
        actionManager.registerAction("toggle_rapid_voxel_testing",new AbstractAction() {

            private boolean active = false;
            private final Random rand = new Random();
            private LifeTimeThread thread;

            private final int size = 9;
            private final int perTick = 1;

            @Override
            public void actionPerformed(ActionEvent e) {
                active = !active;
                if (active) {
                    thread = new LifeTimeThread() {
                        private void toggle() {
                            int[] pos = new int[] {rand.nextInt(size) - size/2, rand.nextInt(size) - size/2, rand.nextInt(size) - size/2};
                            Voxel voxel = data.searchVoxel(pos, false);
                            if (voxel == null) {
                                data.addVoxel(new Color(rand.nextInt()), null, pos);
                            } else {
                                data.removeVoxel(voxel.id);
                            }
                        }
                        @Override
                        public void loop() throws InterruptedException {
                            asyncActionManager.addAsyncAction(new AsyncAction() {
                                @Override
                                public void performAction() {
                                    for (int i = 0; i < perTick; i++) {
                                        toggle();
                                    }
                                }
                            });
                            synchronized (this) {
                                thread.wait(50);
                            }
                        }
                    };
                    threadManager.manage(thread);
                    console.addLine("Test activated.");
                } else {
                    threadManager.remove(thread);
                    console.addLine("Test deactivated.");
                }
            }
        });

        // holds all console actions
        final HashMap<String, String> consoleAction = new HashMap<String, String>();
        // needs to be all lower case
        consoleAction.put("/clear", "console_action_clear");
        consoleAction.put("/debug exception", "create_error_for_debug");
        consoleAction.put("/study", "study_object_complexity");
        consoleAction.put("/check update", "force_update_check");
        consoleAction.put("/test voxel", "toggle_rapid_voxel_testing");
        consoleAction.put("/test camera", "toggle_rapid_camera_testing");
        consoleAction.put("/texture", "texture_debug_information");
        consoleAction.put("/shader", "toggle_shader_enabled");
        consoleAction.put("/check deadlock", "check_for_deadlock_toggle");
        consoleAction.put("/study holes", "study_holes_print_info");
        consoleAction.put("/help", "display_console_commands");

        actionManager.registerAction("display_console_commands", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                console.addLine("Available Commands:");
                for (String action : consoleAction.keySet()) {
                    console.addLine("  " + action);
                }
                console.addLine("-------------------");
            }
        });

        // check current content for holes and print info
        actionManager.registerAction("study_holes_print_info", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                HullManagerExt<Integer> hullManager = new HullManagerExt<Integer>();
                for (Voxel voxel : data.getVisibleLayerVoxel()) {
                    hullManager.update(voxel.posId, null);
                }
                boolean interiorFound = hullManager.computeExterior();
                if (interiorFound) {
                    console.addLine("Holes were detected.");
                    console.addLine("Voxel faces with holes: " +
                            (hullManager.getHull(0).length + hullManager.getHull(1).length +
                                    hullManager.getHull(2).length + hullManager.getHull(3).length +
                                    hullManager.getHull(4).length + hullManager.getHull(5).length));
                    console.addLine("Voxel faces without holes: " +
                            (hullManager.getExteriorHull(0).length + hullManager.getExteriorHull(1).length +
                                    hullManager.getExteriorHull(2).length + hullManager.getExteriorHull(3).length +
                                    hullManager.getExteriorHull(4).length + hullManager.getExteriorHull(5).length));
                } else {
                    console.addLine("No holes were detected.");
                }
            }
        });

        // display the currently loaded textures
        actionManager.registerAction("texture_debug_information", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                asyncActionManager.addAsyncAction(new AsyncAction() {
                    @Override
                    public void performAction() {
                        TextureManager manager = TextureManager.getInstance();
                        console.addLine("Loaded Texture Information (" + manager.getTextureCount() + "):");
                        HashMap<Point, Integer> amounts = new HashMap<Point, Integer>();
                        HashMap<Point, Long> sizes = new HashMap<Point, Long>();

                        for (Enumeration names = manager.getNames(); names.hasMoreElements();) {
                            String name = names.nextElement().toString();
                            Texture texture = manager.getTexture(name);
                            Point dim = new Point(texture.getWidth(), texture.getHeight());
                            Integer count = amounts.get(dim); // must not be inline (null pointer exception)
                            if (count == null) {
                                count = 0;
                            }
                            amounts.put(dim, count + 1);
                            Long memUsage = sizes.get(dim);
                            if (memUsage == null) {
                                memUsage = 0L;
                            }
                            memUsage += texture.getMemoryUsage();
                            sizes.put(dim, memUsage);
                        }

                        long sizeSum = 0;

                        for (Map.Entry<Point, Integer> entry : amounts.entrySet()) {
                            int count = entry.getValue();
                            Point dim = entry.getKey();
                            long size = sizes.get(dim);
                            console.addLine("[" + dim.x + "," + dim.y + "]: " + count + " @ " + String.format("%,.1f", size / 1024.0) + " KB");
                            sizeSum += size;
                        }

                        console.addLine("Total Memory Usage: " + String.format("%,.1f", sizeSum / 1048576.0) + " MB");
                    }
                });
            }
        });

        // register all console actions (so debug know that they are used)
        for (String action : consoleAction.values()) {
            actionManager.registerActionIsUsed(action);
        }

        // set textarea colors
        inputField.setBackground(VitcoSettings.DEFAULT_DARK_BG_COLOR);
        inputField.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0,0,1,1,VitcoSettings.DEFAULT_BORDER_COLOR),
                        BorderFactory.createEmptyBorder(3,3,3,3)
                )
        );
        inputField.setForeground(VitcoSettings.SOFT_WHITE);
        inputField.setCaretColor(VitcoSettings.SOFT_WHITE);

        inputField.addKeyListener(new KeyAdapter() {
            // holds previous console commands
            String tmpCommand = null;
            final ArrayList<String> commands = new ArrayList<String>();
            int pos = 0;

            @Override
            public void keyPressed(final KeyEvent e) {
                super.keyPressed(e);
                switch (e.getKeyCode()) {
                    case 10:
                        String command = inputField.getText().toLowerCase().trim();
                        final String action = consoleAction.get(command);
                        if (action != null) {
                            console.addLine(command); // notify about execution
                            commands.remove(command); // delete older position
                            commands.add(command); // add to the end
                            tmpCommand = null; // reset the tmp command
                            if (commands.size() > 10) { // keep the history to 10
                                commands.remove(0);
                            }
                            pos = commands.size(); // latest position
                            inputField.setText(""); // remove command
                            actionManager.getAction(action).actionPerformed( // execute command
                                    new ActionEvent(e.getSource(), e.hashCode(), e.toString())
                            );
                        }
                        break;
                    case KeyEvent.VK_UP: // previous command
                        if (tmpCommand == null) {
                            tmpCommand = inputField.getText().toLowerCase().trim();
                        }
                        if (commands.size() > 0 && pos > 0) {
                            inputField.setText(commands.get(pos-1));
                            pos--;
                        }
                        break;
                    case KeyEvent.VK_DOWN: // next command
                        if (pos + 1 < commands.size()) {
                            inputField.setText(commands.get(pos + 1));
                            pos++;
                        } else if (tmpCommand != null) {
                            inputField.setText(tmpCommand);
                            tmpCommand = null;
                            pos++;
                        }
                        break;
                    default: break;
                }
            }
        });

        final JPanel consolePanel = new JPanel();
        consolePanel.setLayout(new BorderLayout());
        consolePanel.add(scrollPane, BorderLayout.CENTER);
        consolePanel.add(inputField, BorderLayout.SOUTH);

        // the console itself is in the middle
        panel.add(consolePanel, BorderLayout.CENTER);
        // create menu bar to the left
        CommandMenuBar menuPanel = new CommandMenuBar();
        menuPanel.setOrientation(1); // top down orientation
        menuGenerator.buildMenuFromXML(menuPanel, "com/vitco/layout/content/console/toolbar.xml");
        panel.add(menuPanel, BorderLayout.WEST);

        // register toggle actions (auto show / auto scroll)
        StateActionPrototype toggleAutoShow = new StateActionPrototype() {
            @Override
            public boolean getStatus() {
                return scrollPane.autoShow;
            }

            @Override
            public void action(ActionEvent e) {
                scrollPane.autoShow = !scrollPane.autoShow;
                preferences.storeBoolean("console_auto_show_status", scrollPane.autoShow); // store in pref
                console.addLine(
                        "Console Auto Show is " + (scrollPane.autoShow ? "enabled." : "disabled.")
                );
            }
        };
        StateActionPrototype toggleAutoScroll = new StateActionPrototype() {
            @Override
            public boolean getStatus() {
                return scrollPane.autoScroll;
            }

            @Override
            public void action(ActionEvent e) {
                scrollPane.autoScroll = !scrollPane.autoScroll;
                preferences.storeBoolean("console_auto_scroll_status", scrollPane.autoScroll); // store in pref
                console.addLine(
                        "Console Auto Scroll is " + (scrollPane.autoScroll ? "enabled." : "disabled.")
                );
            }
        };
        actionManager.registerAction("console_toggle_auto_show", toggleAutoShow);
        actionManager.registerAction("console_toggle_auto_scroll", toggleAutoScroll);

        // register clear action
        AbstractAction clearConsole = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                console.clear(); // the console
                textArea.setText(""); // what we display
            }
        };
        actionManager.registerAction("console_action_clear", clearConsole);

        return panel;
    }

}
