package vitco.helper;

import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.awt.*;
import java.io.File;
import java.util.HashMap;

/**
 * Read the contents of the linkage xml file.
 */
public class XmlLinkageReader {

    private final HashMap<String, String[]> maps = new HashMap<String, String[]>();

    private int width = -1;
    private int height = -1;

    public final Point getSize() {
        return new Point(width+1, height+1);
    }

    public final String[] getMapFile(int x, int y) {
        return maps.get(x + "_" + y);
    }

    public XmlLinkageReader(String filename) {
        try {

            File fXmlFile = new File(filename);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(fXmlFile);
            doc.getDocumentElement().normalize();

            NodeList nList = doc.getElementsByTagName("map");

            for (int temp = 0; temp < nList.getLength(); temp++) {

                Element eElement = (Element) nList.item(temp);

                maps.put(
                        eElement.getAttribute("x") + "_" + eElement.getAttribute("y"),
                        new String[] {
                                eElement.getAttribute("folder") + "\\",
                                eElement.getAttribute("file")
                        }
                );

                width = Math.max(Integer.valueOf(eElement.getAttribute("x")),width);
                height = Math.max(Integer.valueOf(eElement.getAttribute("y")),height);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
