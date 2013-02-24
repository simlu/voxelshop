package vitco.group;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.HashSet;
import java.util.Set;

/*
* Represents a tile image. Easy access to the colors in this image.
*
 */

public class TileRep {
	// holds all the colors that are in this image
	private final Set<Integer> col = new HashSet<Integer>();
	// holds the tile data
	public final int id;

	// get all the colors
	public Set<Integer> getCol() {
		return col;
	}

	// construct this tile representation
	public TileRep(BufferedImage img, int id) {
		this.id = id;
		// height and width
		int width = img.getWidth();
		int height = img.getHeight();

		// pixel array
		int pixels[] = ((DataBufferInt) (img).getRaster().getDataBuffer())
				.getData();

		// find all unique colors
		for (int y = 0; y < width; y++) {
			for (int x = 0; x < height; x++) {
				Integer c = pixels[y * width + x];
				if (!col.contains(c)) {
					col.add(c);
				}
			}
		}
	}
}
