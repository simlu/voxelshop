package vitco.helper;

import com.sixlegs.png.PngImage;
import com.sun.org.apache.xml.internal.security.exceptions.Base64DecodingException;
import com.sun.org.apache.xml.internal.security.utils.Base64;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

/**
 * Reads the contents of a map tmx file.
 *
 * Format reference:
 * https://github.com/bjorn/tiled/wiki/TMX-Map-Format
 */
public class TmxMapReader {
    // default map size
    int defaultMapWidth = 128;
    int defaultMapHeight = 96;

    // default tile size
    int tileSize = 32;

    private byte[] walkable = new byte[defaultMapWidth * defaultMapHeight];
    public byte[] getWalkable() {
        return walkable.clone();
    }

    private short[] tileIds = new short[defaultMapWidth * defaultMapHeight];
    public short[] getTileIds() {
        return tileIds.clone();
    }

    // maps tile ids to tiles
    private final HashMap<Integer, BufferedImage> tiles = new HashMap<Integer, BufferedImage>();
    public BufferedImage getTile(int id) {
        return tiles.get(id);
    }

    // =======================
    // handles merged tiles
    private String[] mtileIds = new String[defaultMapWidth * defaultMapHeight];
    // =======================

    // get the maximum tile id
    Integer max = null;
    public final int getMaxTileId() {
        if (max == null) {
            max = 0;
            for (short tileId : tileIds) {
                max = Math.max(max, tileId);
            }
        }
        return max;
    }

    // internal - read the tile layer or the collision layer
    private void readLayer(int type, Element data) throws Base64DecodingException, IOException {
        String encoding = data.getAttribute("encoding");
        if (encoding != null && "base64".equalsIgnoreCase(encoding)) {

            byte[] decodedBytes = Base64.decode(data.getChildNodes().item(0).getTextContent().trim());
            ByteArrayInputStream bais = new ByteArrayInputStream(decodedBytes);
            InputStream is;

            String comp = data.getAttribute("compression");

            if ("gzip".equalsIgnoreCase(comp)) {
                is = new GZIPInputStream(bais);
            } else if ("zlib".equalsIgnoreCase(comp)) {
                is = new InflaterInputStream(bais);
            } else {
                is = bais;
            }

            int i = 0;
            for (int y = 0; y < defaultMapHeight; y++) {
                for (int x = 0; x < defaultMapWidth; x++) {
                    int tileId = 0;
                    tileId |= is.read();
                    tileId |= is.read() << 8;
                    tileId |= is.read() << 16;
                    tileId |= is.read() << 24;

                    switch (type) {
                        case 0: // walkable
                            walkable[i] = tileId == 0 ? (byte)Color.BLACK.getRGB() : (byte)Color.WHITE.getRGB();
                            break;
                        case 1: case 2: // main
                            tileIds[i] = (short)tileId;
                            if (tileId != 0) {
                                mtileIds[i] = mtileIds[i] + String.valueOf(tileId) + "_";
                            }
                            break;
                    }
                    i++;
                }
            }

        } else {
            NodeList nodeList = data.getElementsByTagName("tile");
            for (int i = 0, len = nodeList.getLength(); i < len; i++) {
                Element ele = (Element) nodeList.item(i);
                switch (type) {
                    case 0: // walkable
                        walkable[i] = Short.valueOf(ele.getAttribute("gid")) == 0 ? (byte)Color.BLACK.getRGB() : (byte)Color.WHITE.getRGB();
                        break;
                    case 1: case 2: // main
                        tileIds[i] = Short.valueOf(ele.getAttribute("gid"));
                        if (tileIds[i] != 0) {
                            mtileIds[i] = mtileIds[i] + String.valueOf(tileIds[i]) + "_";
                        }
                        break;
                }
            }
        }
    }

    public TmxMapReader(String filename, String dir) {
        // initialize
        for (int i = 0; i < mtileIds.length; i++) {
            mtileIds[i] = "";
        }

        try {
            File fXmlFile = new File(filename);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(fXmlFile);
            doc.getDocumentElement().normalize();

            // read collision data
            NodeList layers = doc.getElementsByTagName("layer");

            for (int temp = 0; temp < layers.getLength(); temp++) {
                Element eElement = (Element) layers.item(temp);

                { // read tile ids
                    if (eElement.getAttribute("name").equalsIgnoreCase("main")) {
                        // read the tile id data
                        Element data = (Element) eElement.getElementsByTagName("data").item(0);
                        readLayer(1, data);
                    }
                }

                { // read tile ids (overlay)
                    if (eElement.getAttribute("name").toLowerCase().startsWith("overlay")) {
                        // read the collision data
                        Element data = (Element) eElement.getElementsByTagName("data").item(0);
                        readLayer(2, data);
                    }
                }

                { // read walkable tiles
                    if (eElement.getAttribute("name").equalsIgnoreCase("collision")) {
                        // read the collision data
                        Element data = (Element) eElement.getElementsByTagName("data").item(0);
                        readLayer(0, data);
                    }
                }

            }

            // read the tilesets
            NodeList tilesets = doc.getElementsByTagName("tileset");

            // temporary tiles for merging
            HashMap<Integer, BufferedImage> mtiles = new HashMap<Integer, BufferedImage>();

            for (int temp = 0; temp < tilesets.getLength(); temp++) {

                Element eElement = (Element) tilesets.item(temp);
                int curId = Integer.valueOf(eElement.getAttribute("firstgid"));
                Element image = (Element) eElement.getElementsByTagName("image").item(0);
                String imageName = image.getAttribute("source");
                int width = Integer.valueOf(image.getAttribute("width"));
                int height = Integer.valueOf(image.getAttribute("height"));

                // load the image
                BufferedImage img = new PngImage().read(new File(dir + imageName));

                // load the tiles to hashmap
                for (int y = 0, len2 = height/tileSize; y < len2; y++) {
                    for (int x = 0, len = width/tileSize; x < len; x++) {
                        BufferedImage tile = new BufferedImage(tileSize, tileSize, BufferedImage.TYPE_INT_ARGB);
                        Graphics2D gc = (Graphics2D)tile.getGraphics();
                        gc.setComposite(AlphaComposite.Src);
                        gc.drawImage(img.getSubimage(x * tileSize, y * tileSize, tileSize, tileSize), 0, 0, null);
                        mtiles.put(curId++, tile);

                    }
                }
            }

            // finish the merging of tiles
            int uId = 1;
            for (int i = 0; i < mtileIds.length; i++) {
                if (mtileIds[i] != null) {
                    String cTileIds = mtileIds[i];
                    String[] tileIds = mtileIds[i].split("_");
                    // merge all tiles to a new tileId
                    BufferedImage tile = new BufferedImage(tileSize, tileSize, BufferedImage.TYPE_INT_ARGB);
                    Graphics2D gc = (Graphics2D)tile.getGraphics();
                    gc.setComposite(AlphaComposite.SrcOver);
                    for (String tileId : tileIds) {
                        if (!tileId.equals("")) {
                            gc.drawImage(mtiles.get(Integer.valueOf(tileId)), 0, 0, null);
                        }
                    }
                    // add to tile set
                    tiles.put(uId, tile);
                    // update all entries
                    for (int j = i; j < mtileIds.length; j++) {
                        if (mtileIds[j] != null && mtileIds[j].equals(cTileIds)) {
                            mtileIds[j] = null;
                            this.tileIds[j] = (short)uId;
                        }
                    }

                    uId++;
                }
            }

        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (Base64DecodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
