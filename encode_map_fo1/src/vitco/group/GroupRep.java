package vitco.group;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/*
* Represents a group of tiles.
 */

public class GroupRep {
	// holds all the TileRep of this GroupRep
	private ArrayList<TileRep> tileList = new ArrayList<TileRep>();
	// holds all the colors of this group
	private Set<Integer> groupCol = new HashSet<Integer>();

	// get the tiles of this group
	public ArrayList<TileRep> getTiles() {
		return tileList;
	}

	// add a tile to this group
	public void addTile(TileRep tile) {
		tileList.add(tile);
		// add the colors
		Integer tmp;
		Set<Integer> col = tile.getCol();
        for (Integer aCol : col) {
            tmp = aCol;
            if (!groupCol.contains(tmp)) {
                groupCol.add(tmp);
            }
        }
	}

	// get the count of the colors in this group
	public int getColCount() {
		return groupCol.size();
	}

	// get colors of this group
	public Set<Integer> getCol() {
		return groupCol;
	}
}
