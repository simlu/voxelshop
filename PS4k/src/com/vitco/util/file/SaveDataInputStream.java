package com.vitco.util.file;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;

/**
 * To read a file (binary or not)
 */
public class SaveDataInputStream extends DataInputStream {

    /**
     * Creates a DataInputStream that uses the specified
     * underlying InputStream.
     *
     * @param in the specified input stream
     */
    public SaveDataInputStream(InputStream in) {
        super(in);
    }

    // returns null if eof is reached
    public String readLineSave() throws IOException {

        // todo: is this save? - readLine() is not, why?

        StringBuilder lineBuf = new StringBuilder();
        int c1, c2;

        loop: while (true) {
            c1 = in.read();
            switch (c1) {
                case '\n':
                    break loop;
                case -1:
                    return null;
                case '\r':
                    c2 = in.read();
                    if (c2 != '\n' && c2 != -1) {
                        if (!(in instanceof PushbackInputStream)) {
                            this.in = new PushbackInputStream(in);
                        }
                        ((PushbackInputStream)in).unread(c2);
                    }
                    break loop;
                default:
                    lineBuf.append((char)c1);
            }
        }

        return lineBuf.toString();
    }
}
