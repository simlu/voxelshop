package com.vitco.util.file;

import com.googlecode.pngtastic.core.PngChunk;
import com.googlecode.pngtastic.core.PngImage;
import com.googlecode.pngtastic.core.PngOptimizer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;

public class FileOut {
    private final DataOutputStream p;

    // constructor
    public FileOut(String filename) throws FileNotFoundException {
        p = new DataOutputStream(new FileOutputStream(filename));
    }

    // alternative constructor
    public FileOut(ByteArrayOutputStream b) {
        this.p = new DataOutputStream(b);
    }

    // attach another ByteArrayOutputStream
    public void writeBytes(byte[] bytes) throws IOException {
        p.write(bytes);
    }

    // write text
    public void writeASCIIString(String text) throws IOException {
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(p, "ASCII"));
        bw.write(text);
        bw.flush();
    }

    // write text
    public void writeUTF8String(String text) throws IOException {
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(p, "UTF8"));
        bw.write(text);
        bw.flush();
    }

    // write a line of text
    public void writeLine(String text) throws IOException {
        p.writeBytes(text + "\r\n");
    }

    // write integer revered
    public void writeIntRev(int data) throws IOException {
        p.writeInt(Integer.reverseBytes(data));
    }

    // write short reversed
    public void writeShortRev(short data) throws IOException {
        p.writeShort(Short.reverseBytes(data));
    }

    // write float reversed
    public void writeFloatRev(float data) throws IOException {
        p.writeFloat(Float.intBitsToFloat(
                Integer.reverseBytes(Float.floatToIntBits (data))
        ));
    }

    // write byte
    public void writeByte(byte data) throws IOException {
        p.writeByte(data);
    }

    // write bytes
    public void writeBytes(String bytes) throws IOException {
        p.writeBytes(bytes);
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

    // write an image file compressed
    public void writeImageCompressed(BufferedImage img) throws IOException {

        // convert to png
        ByteArrayOutputStream tmp = new ByteArrayOutputStream();
        ImageIO.write(img, "png", tmp);
        tmp.close();

        // compress
        PngImage pngImage = new PngOptimizer().optimize(
                new PngImage(new ByteArrayInputStream(tmp.toByteArray())),
                9
        );

        // extract compressed data as png
        ByteArrayOutputStream compressedPngData = new ByteArrayOutputStream();
        DataOutputStream outputStreamWrapper = new DataOutputStream(compressedPngData);
        outputStreamWrapper.writeLong(PngImage.SIGNATURE);
        for (PngChunk chunk : pngImage.getChunks())
        {
            outputStreamWrapper.writeInt(chunk.getLength());
            outputStreamWrapper.write(chunk.getType());
            outputStreamWrapper.write(chunk.getData());
            int i = (int)chunk.getCRC();
            outputStreamWrapper.writeInt(i);
        }
        outputStreamWrapper.close();
        compressedPngData.close();

        // convert to png byte array
        byte[] data = compressedPngData.toByteArray();
        int contentLength = data.length;

        // write the size
        this.writeIntRev(contentLength);

        // write the data
        p.write(data);
    }

    // finalize
    public void finish() throws IOException {
        p.close();
    }
}
