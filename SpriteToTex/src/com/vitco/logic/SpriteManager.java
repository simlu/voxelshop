package com.vitco.logic;

import com.google.gson.Gson;
import com.sixlegs.png.PngImage;
import com.vitco.logic.container.SpriteObject;
import com.vitco.logic.container.TexSheetPos;
import com.vitco.tools.FileTools;
import com.vitco.tools.HexTools;
import com.vitco.tools.MySQLConnection;
import com.vitco.tools.Zip;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

/**
 * Manages the sprites and puts them into files.
 */
public class SpriteManager {

    // needs to be 2 ^ X s.t. X ^ 2 geq the max sprite dimension
    private static final int maxTexSheetSize = 1024;

    // holds all sprite objects
    private final ArrayList<SpriteObject> sprites = new ArrayList<SpriteObject>();

    // maps the map ids to the sprite sheets
    private final HashMap<Short, ArrayList<String[]>> imageList =
            new HashMap<Short, ArrayList<String[]>>();

    // constructor, will load the data we need to process (from mysql)
    public SpriteManager() {
        try {
            Connection connection = MySQLConnection.getInstance(); // closes by itself
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery("SELECT \n" +
                    "    prefix, postfix, type, \n" +
                    "    id, sprite, zone\n" +
                    "FROM\n" +
                    "    # item icons\n" +
                    "    ((SELECT\n" +
                    "        'images/item_icons/' as prefix,\n" +
                    "        '-icon.png' as postfix,\n" +
                    "        'itemicon' as type,\n" +
                    "        id,\n" +
                    "        sprite,\n" +
                    "        0 as zone\n" +
                    "    FROM\n" +
                    "        fmofP.aafoitem)\n" +
                    "    UNION ALL\n" +
                    "    # item sprites\n" +
                    "    (SELECT\n" +
                    "        'images/c/' as prefix,\n" +
                    "        '.png' as postfix,\n" +
                    "        'itemsprite' as type,\n" +
                    "        id,\n" +
                    "        sprite,\n" +
                    "        0 as zone\n" +
                    "    FROM\n" +
                    "        fmofP.aafoitem)\n" +
                    "    UNION ALL\n" +
                    "    # achievements icons\n" +
                    "    (SELECT\n" +
                    "        'images/achievements/' as prefix,\n" +
                    "        '.png' as postfix,\n" +
                    "        'archicon' as type,\n" +
                    "        id,\n" +
                    "        CAST(id AS CHAR) as sprite,\n" +
                    "        0 as zone\n" +
                    "    FROM\n" +
                    "        fmofm.aafoachievements)\n" +
                    "    UNION ALL\n" +
                    "    # npc sprites\n" +
                    "    (SELECT\n" +
                    "        'images/c/' as prefix,\n" +
                    "        '.png' as postfix,\n" +
                    "        'npc' as type,\n" +
                    "        id,\n" +
                    "        sprite,\n" +
                    "        zone+1 as zone\n" +
                    "    FROM\n" +
                    "        fmofP.aafonpc)\n" +
                    "    UNION ALL\n" +
                    "    # mob sprites\n" +
                    "    (SELECT\n" +
                    "        'images/c/' as prefix,\n" +
                    "        '.png' as postfix,\n" +
                    "        'mob' as type,\n" +
                    "        id,\n" +
                    "        sprite,\n" +
                    "        zone+1 as zone\n" +
                    "    FROM\n" +
                    "        fmofP.aafomob)) T1\n" +
                    "ORDER BY # make sure it's always the same order\n" +
                    "    zone, type, id");
            while (rs.next()) {
                sprites.add(new SpriteObject(
                        rs.getString("prefix"), rs.getString("postfix"),
                        rs.getString("type"), rs.getInt("id"),
                        rs.getString("sprite"), rs.getShort("zone")));
            }
            rs.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // generate all files
    public void generateFiles(String dir) {
        short lastZone = -1;
        String lastType = "";
        ArrayList<SpriteObject> mapSprites = new ArrayList<SpriteObject>();

        // clear the mobile folder
        File outDir = new File(dir + "images/mobile/");
        if (outDir.exists()) {
            FileTools.emptyDir(outDir);
        } else {
            if (!outDir.mkdir()) {
                System.err.println("Error: Unable to create \"mobile\" directory.");
            }
        }

        for (int i = 0; i < sprites.size(); i++) {
            SpriteObject obj = sprites.get(i);
            File file = new File(dir + obj.prefix + obj.sprite + obj.postfix);
            if (file.exists()) { // only consider those items where the sprite exists
                // add to group
                if ((lastZone != obj.zone || !lastType.equals(obj.type)) && mapSprites.size() > 0) {
                    // we need to process this group
                    processGroup(mapSprites, lastZone, lastType, dir);
                }
                // add
                lastType = obj.type;
                lastZone = obj.zone;
                obj.setSpriteFile(file);
                mapSprites.add(obj);
            } else {
                //System.err.println("Error: Sprite \"" + obj.sprite + "\" not found.");
                sprites.remove(i);
                i--;
            }
        }

        // create the json file (and zipped version)
        try {
            File jsonFile = new File(dir + "images/mobile/info.json");
            if (!jsonFile.exists()) {
                if (!jsonFile.createNewFile()) {
                    System.err.println("Error: Can not create json file...");
                }
            }
            BufferedWriter out = new BufferedWriter(new FileWriter(jsonFile));
            out.write(new Gson().toJson(new Object[] {imageList, sprites}));
            out.close(); // also flushes
            // write a zipped version
            Zip.pack(dir + "images/mobile/info.json.zip", dir + "images/mobile/info.json");
        } catch (IOException e) {
            e.printStackTrace();
        }

        // (re)create zip file
        //noinspection ResultOfMethodCallIgnored
        try {
            Zip.zip(new File(dir + "images/mobile/"), new File(dir + "images/initial_download.zip"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // process image groups
    private void processGroup(ArrayList<SpriteObject> mapSprites, short zone, String type, String dir) {
        // get the size of the first image to do calculations
        int width = 0, height = 0;
        if (type.equals("npc")) {
            // npc sprites need to be cropped
            width = 24;
            height = 32;
        } else {
            try {
                BufferedImage img = ImageIO.read(mapSprites.get(0).file);
                width = img.getWidth();
                height = img.getHeight();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        int sheetCount = 1;
        int lastSize = 128;
        while (!mapSprites.isEmpty()) {
            // generate image and file name
            BufferedImage texSheet = new BufferedImage(maxTexSheetSize, maxTexSheetSize, BufferedImage.TYPE_INT_ARGB);
            String fileName = zone + "-" + type + "-" + sheetCount++ + ".png";
            // generate all valid positions in 1024^2 tile sheet
            ArrayList<TexSheetPos> texSheetPositions = new ArrayList<TexSheetPos>();
            for (int x = 0; x + width <= maxTexSheetSize; x += width) {
                for (int y = 0; y + width <= maxTexSheetSize; y += height) {
                    int xIn = (int)Math.pow(2, (int) Math.ceil(Math.log(x + width) / Math.log(2)));
                    int yIn = (int)Math.pow(2, (int) Math.ceil(Math.log(y + height) / Math.log(2)));
                    int positionIn = Math.max(xIn, yIn);
                    texSheetPositions.add(new TexSheetPos(positionIn, new Point(x,y)));
                }
            }
            Collections.sort(texSheetPositions, new Comparator<TexSheetPos>() {
                @Override
                public int compare(TexSheetPos o1, TexSheetPos o2) {
                    return o1.positionIn - o2.positionIn;
                }
            });
            while (!texSheetPositions.isEmpty() && !mapSprites.isEmpty()) {
                TexSheetPos texSheetPos = texSheetPositions.remove(0);
                SpriteObject spriteObject = mapSprites.remove(0);
                spriteObject.setPosition(texSheetPos.point);
                spriteObject.setTexture(fileName);
                lastSize = texSheetPos.positionIn;
                // copy on image
                try {
                    // load the image
                    BufferedImage pngImage = new PngImage().read(spriteObject.file);
                    if (type.equals("npc")) {
                        // crop to correct size if npc type
                        pngImage = pngImage.getSubimage(24, 64, 24, 32);
                    }
                    texSheet.getGraphics().drawImage(pngImage,texSheetPos.point.x, texSheetPos.point.y, null);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            // crop the image
            texSheet = texSheet.getSubimage(0, 0, lastSize, lastSize);
            // write the file
            try {
                File result = new File(dir + "images/mobile/" + fileName);
                ImageIO.write(texSheet, "png", result);
            } catch (IOException e) {
                e.printStackTrace();
            }
            // compute the md5 hash
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            try {
                ImageIO.write(texSheet, "png", os);
                MessageDigest md = MessageDigest.getInstance("MD5");
                md.update(os.toByteArray());
                byte[] hash = md.digest();
                String md5 = HexTools.byteToHex(hash);
                // save information
                ArrayList<String[]> list = imageList.get(zone);
                if (list == null) {
                    list = new ArrayList<String[]>();
                    imageList.put(zone, list);
                }
                list.add(new String[] {fileName, md5});
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace(); // should never happen
            } catch (IOException e) {
                e.printStackTrace(); // should also never happen
            }

        }
    }

}
