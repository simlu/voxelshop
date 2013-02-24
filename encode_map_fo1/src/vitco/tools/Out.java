package vitco.tools;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;

public class Out {
	// internal
	private FileOutputStream out;
	private DataOutputStream p;

	// constructor
	public Out(String filename) throws FileNotFoundException {
		out = new FileOutputStream(filename);
		p = new DataOutputStream(out);
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
		byte[] bytes = FileTools.getBytesFromFile(file);
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
