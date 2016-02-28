package com.vitco;

import com.vitco.layout.content.menu.MainMenuLogic;
import com.vitco.layout.content.shortcut.ShortcutManager;
import com.vitco.manager.action.ActionManager;
import com.vitco.manager.action.ComplexActionManager;
import com.vitco.manager.error.ErrorHandler;
import com.vitco.manager.pref.Preferences;
import com.vitco.settings.VitcoSettings;
import com.vitco.util.misc.SaveResourceLoader;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.awt.*;
import java.io.File;

/**
 * Initially executed class
 */

public class Main {

    private static boolean debug = false;

    // true if the program runs in debug mode
    public static boolean isDebugMode() {
        return debug;
    }

    public static void main(String[] args) throws Exception {
        // display version number on splash screen
        final SplashScreen splash = SplashScreen.getSplashScreen();
        if (splash != null) {
            Graphics2D g = splash.createGraphics();
            if (g != null) {
                g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                        RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
                Font font = new Font("Verdana", Font.BOLD, 7);
                g.setFont(font);
                //g.setFont(g.getFont().deriveFont(9f));
                g.setColor(VitcoSettings.SPLASH_SCREEN_OVERLAY_TEXT_COLOR);
                g.drawString("V" + VitcoSettings.VERSION_ID, 10, 15);
                splash.update();
                g.dispose();
            }
        }

        // the JIDE license
        SaveResourceLoader saveResourceLoader = new SaveResourceLoader("resource/jidelicense.txt");
        if (!saveResourceLoader.error) {
            String[] jidelicense = saveResourceLoader.asLines();
            if (jidelicense.length == 3) {
                com.jidesoft.utils.Lm.verifyLicense(jidelicense[0], jidelicense[1], jidelicense[2]);
            }
        }

        // check if we are in debug mode
        if ((args.length > 0) && args[0].equals("debug")) {
            ErrorHandler.setDebugMode();
            debug = true;
        }

        // build the application
        final ConfigurableApplicationContext context = new ClassPathXmlApplicationContext("com/vitco/glue/config.xml");

        // for debugging
        if (debug) {
            ((ActionManager) context.getBean("ActionManager")).performValidityCheck();
            ((ComplexActionManager) context.getBean("ComplexActionManager")).performValidityCheck();
        }

        // open vsd file when program is started with "open with"
        MainMenuLogic mainMenuLogic = ((MainMenuLogic) context.getBean("MainMenuLogic"));
        for (String arg : args) {
            if (arg.endsWith(".vsd")) {
                File file = new File(arg);
                if (file.exists() && !file.isDirectory()) {
                    mainMenuLogic.openFile(file);
                    break;
                }
            }
        }

        // perform shortcut check
        ((ShortcutManager) context.getBean("ShortcutManager")).doSanityCheck(debug);
//        // test console
//        final Console console = ((Console) context.getBean("Console"));
//        new Thread() {
//            public void run() {
//                while (true) {
//                    console.addLine("text");
//                    try {
//                        sleep(2000);
//                    } catch (InterruptedException e) {
//                       //e.printStackTrace();
//                    }
//                }
//            }
//        }.start();

        // add a shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                // make reference so Preferences object doesn't get destroyed
                Preferences pref = ((Preferences) context.getBean("Preferences"));
                // trigger @PreDestroy
                context.close();
                // store the preferences (this needs to be done here, b/c
                // some PreDestroys are used to store preferences!)
                pref.save();
            }
        });
    }
}
