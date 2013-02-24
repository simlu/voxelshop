package vitco.tools;

import java.io.*;
import java.net.URI;
import java.util.Deque;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class Zip {
	// some variables
	static final int BUFFER = 2048;

    private static void copy(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        while (true) {
            int readCount = in.read(buffer);
            if (readCount < 0) {
                break;
            }
            out.write(buffer, 0, readCount);
        }
    }

    private static void copy(File file, OutputStream out) throws IOException {
        InputStream in = new FileInputStream(file);
        try {
            copy(in, out);
        } finally {
            in.close();
        }
    }

    private static void copy(InputStream in, File file) throws IOException {
        OutputStream out = new FileOutputStream(file);
        try {
            copy(in, out);
        } finally {
            out.close();
        }
    }

    public static void zip(File directory, File zipfile) throws IOException {
        URI base = directory.toURI();
        Deque<File> queue = new LinkedList<File>();
        queue.push(directory);
        OutputStream out = new FileOutputStream(zipfile);
        Closeable res = out;
        try {
            ZipOutputStream zout = new ZipOutputStream(out);
            res = zout;
            while (!queue.isEmpty()) {
                directory = queue.pop();
                for (File kid : directory.listFiles()) {
                    String name = base.relativize(kid.toURI()).getPath();
                    if (kid.isDirectory()) {
                        queue.push(kid);
                        name = name.endsWith("/") ? name : name + "/";
                        zout.putNextEntry(new ZipEntry(name));
                    } else {
                        zout.putNextEntry(new ZipEntry(name));
                        copy(kid, zout);
                        zout.closeEntry();
                    }
                }
            }
        } finally {
            res.close();
        }
    }

    public static void unzip(File zipfile, File directory) throws IOException {
        ZipFile zfile = new ZipFile(zipfile);
        Enumeration<? extends ZipEntry> entries = zfile.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            File file = new File(directory, entry.getName());
            if (entry.isDirectory()) {
                if (!file.mkdirs()) {
                    System.err.println("Error: Failed to create directory \"" + file.getAbsolutePath() + "\"!");
                }
            } else {
                if (!file.getParentFile().mkdirs()) {
                    System.err.println("Error: Failed to create directory \"" + file.getParentFile().getAbsolutePath() + "\"!");
                }
                InputStream in = zfile.getInputStream(entry);
                try {
                    copy(in, file);
                } finally {
                    in.close();
                }
            }
        }
    }

	// pack into a zip file
	public static void pack(String zipfile, String filename) {
		try {
			BufferedInputStream origin;
			FileOutputStream dest = new FileOutputStream(zipfile);
			ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(
					dest));
			byte data[] = new byte[BUFFER];
			FileInputStream fi = new FileInputStream(filename);
			origin = new BufferedInputStream(fi, BUFFER);
			ZipEntry entry = new ZipEntry(filename);
			out.putNextEntry(entry);
			int count;
			while ((count = origin.read(data, 0, BUFFER)) != -1) {
				out.write(data, 0, count);
			}
			origin.close();
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// extract a zip file
	public static void extract(String zipfile) throws IOException {
		// extract the zip file
		ZipInputStream zinstream = new ZipInputStream(new FileInputStream(
				zipfile));
		ZipEntry zentry = zinstream.getNextEntry();
		byte[] buf = new byte[1024];
		while (zentry != null) {
			FileOutputStream outstream = new FileOutputStream(zentry.getName());
			int n;
			while ((n = zinstream.read(buf, 0, 1024)) > -1) {
				outstream.write(buf, 0, n);
			}
			outstream.close();
			zinstream.closeEntry();
			zentry = zinstream.getNextEntry();
		}
		zinstream.close();
	}
}
