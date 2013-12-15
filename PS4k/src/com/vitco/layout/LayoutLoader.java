package com.vitco.layout;

import com.jidesoft.plaf.LookAndFeelFactory;
import com.vitco.util.misc.SaveResourceLoader;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.swing.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

/**
 * Load the layout from an xml file
 */
public class LayoutLoader {
    public static void loadLayoutFile(final String xmlFile) {
        // load the layout
        LookAndFeelFactory.installDefaultLookAndFeel();

        LookAndFeelFactory.addUIDefaultsCustomizer(new LookAndFeelFactory.UIDefaultsCustomizer() {
            public void customize(UIDefaults uiDefaults) {
//                for (Map.Entry<Object, Object> entry : uiDefaults.entrySet()) {
//                    if (entry.getValue() instanceof Color) {
//                        uiDefaults.put(entry.getKey(), Color.BLACK);
//                    }
//                }

                try {
                    // load the xml document
                    DocumentBuilderFactory factory = DocumentBuilderFactory
                            .newInstance();
                    DocumentBuilder builder = factory.newDocumentBuilder();
                    Document doc = builder.parse(
                            new SaveResourceLoader(xmlFile).asInputStream()
                    );
                    NodeList list = doc.getFirstChild().getChildNodes();
                    for (int i = 0; i < list.getLength(); i++) {
                        Node node = list.item(i);
                        if (node.getNodeName().equals("entry")) {
                            // set the panel background color
                            Element e = (Element) node;
                            uiDefaults.put(e.getAttribute("key"), e.getNodeValue());
                        }
                    }
                } catch (SAXException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ParserConfigurationException e) {
                    e.printStackTrace();
                }

            }
        });
    }
}
