package com.vitco.util.xml;

import com.vitco.util.error.ErrorHandlerInterface;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
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
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Encapsulates a xml file
 */
public class XmlFile {
    private Document doc;

    // get the direct node children of an element as NodeList
    // that have a name equal to name
    public static NodeList getDirectChildren(Element parent, String name)
    {
        final List<Node> list = new LinkedList<Node>();
        NodeList nodeList = new NodeList() {
            @Override
            public Node item(int index) {
                return list.get(index);
            }

            @Override
            public int getLength() {
                return list.size();
            }
        };
        for(Node child = parent.getFirstChild(); child != null; child = child.getNextSibling())
        {
            if(child instanceof Element && name.equals(child.getNodeName())) {
                list.add(child);
            }
        }
        return nodeList;
    }

    // pattern
    final Pattern datePatt = Pattern.compile("(.+?)(\\[)((\\-)?[0-9]+?)(\\])");

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

            // find the identification
            Integer pos = null;
            String name = dir;
            Matcher m = datePatt.matcher(dir);
            if (m.matches()) {
                pos = Integer.valueOf(m.group(3));
                name = m.group(1);
            }

            NodeList list = getDirectChildren(cur, name);
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

    // go up one step
    public boolean goUp() {
        if (curTop != doc.getDocumentElement()) {
            curTop = (Element) curTop.getParentNode();
            return true;
        } else {
            return false;
        }
    }

    // go up "level" steps
    public boolean goUp(int level) {
        int clevel = level;
        boolean result = true;
        while (clevel > 0 && result) {
            result = goUp();
            clevel--;
        }
        return result;
    }

    // delete the current node and go up
    public boolean deleteChild(String child) {
        boolean result = false;
        // find the identification
        Integer pos = null;
        String name = child;
        Matcher m = datePatt.matcher(child);
        if (m.matches()) {
            pos = Integer.valueOf(m.group(3));
            name = m.group(1);
        }

        NodeList list = getDirectChildren(curTop, name);
        int length = list.getLength();

        if (pos == null) { // position not set
            if (length > 0) { // exists (take the first)
                curTop.removeChild(list.item(0));
                result = true;
            }
        } else { // position is set
            if (pos >= 0) { // position valid
                if (length > pos) { // can select
                    curTop.removeChild(list.item(pos));
                    result = true;
                }
            }
        }
        return result;
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
        boolean result = false;

        try {
            DOMSource domSource = new DOMSource(doc);
            Writer out = new OutputStreamWriter(new FileOutputStream(file), "UTF8");

            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.transform(domSource, new StreamResult(out));

            out.close();
            result = true;
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
