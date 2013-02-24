package com.vitco.util.xml;

import com.vitco.util.error.ErrorHandlerInterface;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Encapsulates a xml file
 */
public class XmlFile {
    private Document doc;

    // pattern
    Pattern datePatt = Pattern.compile("(.+?)(\\[)((\\-)?[0-9]+?)(\\])");

    // current top node
    private Element curTop;

    // create root node as well
    public XmlFile(String rootNode) {
        try {
            DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
            doc = docBuilder.newDocument();
            doc.setXmlStandalone(true);
            doc.appendChild(doc.createElement(rootNode));
            resetTopNode();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
    }

    // set the current top element (all actions will be relative to this node!)
    public void setTopNode(String path) {
        curTop = _createPath(path);
    }

    // set the current top element (all actions will be relative to this node!)
    public void resetTopNode(String path) {
        resetTopNode();
        curTop = _createPath(path);
    }

    // reset the current top element
    public void resetTopNode() {
        curTop = doc.getDocumentElement();
    }

    // internal - creates a path
    private Element _createPath(String path) {
        // split our array and loop over it
        Element cur = curTop;
        if (path.equals("")) {
            return cur;
        }
        String[] pathArray = path.split("/");
        for (String dir : pathArray) {

            // Find the identification
            Integer pos = null;
            String name = dir;
            Matcher m = datePatt.matcher(dir);
            if (m.matches()) {
                pos = Integer.valueOf(m.group(3));
                name = m.group(1);
            }

            NodeList list = cur.getElementsByTagName(name);
            int length = list.getLength();

            if (pos == null) { // position not set
                if (length > 0) { // exists (take the first)
                    cur = (Element) list.item(0);
                } else { // doesn't exists (create)
                    Element newCur = doc.createElement(name);
                    cur.appendChild(newCur);
                    cur = newCur;
                }
            } else { // position is set
                if (pos >= 0) { // position wants to select
                    if (length > pos) { // can select
                        cur = (Element) list.item(pos);
                    } else { // can not select
                        return null;
                    }
                } else { // position is negative (wants to create!)
                    Element newCur = doc.createElement(name);
                    cur.appendChild(newCur);
                    cur = newCur;
                }
            }
        }

        return cur;
    }

    // creates a path (public)
    public boolean createPath(String path) {
        return _createPath(path) == null;
    }

    // creates a path and adds text content
    public boolean addTextContent(String path, String value) {
        Element result = _createPath(path);
        if (result == null) {
            return false;
        } else {
            result.appendChild(doc.createTextNode(value));
            return true;
        }
    }

    // create a path and adds attributes
    public boolean addAttributes(String path, String[] attrs) {
        Element result = _createPath(path);
        if (result == null) {
            return false;
        } else {
            for (String attr : attrs) {
                String[] toSet = attr.split("=", 2);
                result.setAttribute(toSet[0], toSet[1]);
            }
            return true;
        }
    }

    // create a path, adds attributes and text content
    public boolean addAttrAndTextContent(String path, String[] attrs, String value) {
        Element result = _createPath(path);
        if (result == null) {
            return false;
        } else {
            for (String attr : attrs) {
                String[] toSet = attr.split("=", 2);
                result.setAttribute(toSet[0], toSet[1]);
            }
            result.appendChild(doc.createTextNode(value));
            return true;
        }
    }

    // write this xml document as file(name)
    public boolean writeToFile(String filename, ErrorHandlerInterface errorHandler) {
        return writeToFile(new File(filename), errorHandler);
    }

    // write this xml document to file
    public boolean writeToFile(File file, ErrorHandlerInterface errorHandler) {
        boolean result = true;

        try {
            DOMSource domSource = new DOMSource(doc);
            Writer out = new OutputStreamWriter(new FileOutputStream(file), "UTF8");

            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.transform(domSource, new StreamResult(out));

            out.close();
        } catch (FileNotFoundException e) {
            errorHandler.handle(e);
        } catch (TransformerException e) {
            errorHandler.handle(e);
        } catch (IOException e) {
            errorHandler.handle(e);
        }

        return result;
    }
}
