package com.vitco.util.file;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;

public class FileOut {
    private final DataOutputStream p;

    // constructor
    public FileOut(String filename) throws FileNotFoundException {
        p = new DataOutputStream(new FileOutputStream(filename));
    }

    // write text
    public void writeString(String text) throws IOException {
        p.writeBytes(text);
    }

    // write a line of text
    public void writeLine(String text) throws IOException {
        p.writeBytes(text + "\r\n");
    }

    // write integer revered
    public void writeIntRev(int data) throws IOException {
        p.writeInt(Integer.reverseBytes(data));
    }

    // write short revered
    public void writeShortRev(short data) throws IOException {
        p.writeShort(Short.reverseBytes(data));
    }

    // write byte
    public void writeByte(byte data) throws IOException {
        p.writeByte(data);
    }

    // write an image file
    public void writeImage(BufferedImage img) throws IOException {
        // get the size
        ByteArrayOutputStream tmp = new ByteArrayOutputStream();
        ImageIO.write(img, "png", tmp);
        tmp.close();
        Integer contentLength = tmp.size();
        // write the size
        this.writeIntRev(contentLength);
        // write the data
        p.write(tmp.toByteArray());
    }

    // write an image file
    // todo: this method still needs testing!
    public void writeFile(File file) throws IOException {
        // get the bytes
        RandomAccessFile f = new RandomAccessFile(file, "rw");
        byte[] bytes = new byte[(int)f.length()];
        f.read(bytes);

        // get the size
        Integer contentLength = bytes.length;
        // write the size
        this.writeIntRev(contentLength);
        // write the data
        p.write(bytes);
    }

    // finalize
    public void finish() throws IOException {
        p.close();
    }
}
