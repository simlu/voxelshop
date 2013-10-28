package com.vitco.tools;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;

public class Out {
    private DataOutputStream p;

    // Returns the contents of the file in a byte array.
    private static byte[] getBytesFromFile(File file) throws IOException {
        InputStream is = new FileInputStream(file);

        // Get the size of the file
        long length = file.length();

        // You cannot create an array using a long type.
        // It needs to be an int type.
        // Before converting to an int type, check
        // to ensure that file is not larger than Integer.MAX_VALUE.
        if (length > Integer.MAX_VALUE) {
            // File is too large
            System.err.println("Error: File is to large.");
        }

        // Create the byte array to hold the data
        byte[] bytes = new byte[(int)length];

        // Read in the bytes
        int offset = 0;
        int numRead;
        while (offset < bytes.length
                && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
            offset += numRead;
        }

        // Ensure all the bytes have been read in
        if (offset < bytes.length) {
            throw new IOException("Could not completely read file "+file.getName());
        }

        // Close the input stream and return bytes
        is.close();
        return bytes;
    }

	// constructor
	public Out(String filename) throws FileNotFoundException {
		p = new DataOutputStream(new FileOutputStream(filename));
	}
	
	// write text
	public void writeText(String text) throws IOException {
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
	public void writeImage(File file) throws IOException {
		// get the size
		byte[] bytes = getBytesFromFile(file);
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
