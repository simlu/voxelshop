package vitco.group;

import com.google.common.collect.Sets;
import vitco.main.Config;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class TileTools {
	
	// return the amount of overlapping colors
	private static int getOverlapColCount(GroupRep group, TileRep tile) {

		Set<Integer> groupCol = group.getCol();
		java.util.Iterator<Integer> iter = tile.getCol().iterator();

		int dist = 0;

		while (iter.hasNext()) {
			if (groupCol.contains(iter.next())) {
				dist++;
			}
		}

		return dist;
	}
	
	// return the col count if a tile was added to a group
	private static int newColCount(GroupRep group, TileRep tile) {

		Set<Integer> groupCol = group.getCol();
		java.util.Iterator<Integer> iter = tile.getCol().iterator();

		int dist = 0;

		while (iter.hasNext()) {
			if (!groupCol.contains(iter.next())) {
				dist++;
			}
		}

		return group.getColCount() + dist;
	}

    // all groups that were found
	public final static ArrayList<GroupRep> lastGroupList = new ArrayList<GroupRep>();
    // all ids that are used in all groups
	public final static ArrayList<Set<Integer>> lastGroupIdList = new ArrayList<Set<Integer>>();

	// get all groups of tiles with a certain color distance that have at
	// least a certain size
	public static void getGroup(ArrayList<TileRep> tiles, int thres, int minsize, int maxsize, Set<Integer> used) {

		// clear the last group list
		lastGroupList.clear();
		lastGroupIdList.clear();

		// loop over all tiles
		for (int i = 0; i < tiles.size(); i++) {
			if (!used.contains(i) && tiles.get(i).getCol().size() <= thres) {
				// hold the current group ids
				Set<Integer> curGroupIds = new HashSet<Integer>();
				curGroupIds.add(i);
				// hold the current group
				GroupRep curGroup = new GroupRep();
				curGroup.addTile(tiles.get(i));

				// add the tile with the lowest distance
				// until no further tiles can be added
				boolean added = true;
				while (added && curGroupIds.size() < maxsize) {
					added = false;
					boolean valueSet = false;
					int dist = -1;
					int index = -1;
					// ***
					for (Integer j = 0, len = tiles.size(); j < len; j++) {
						if (!curGroupIds.contains(j) && !used.contains(j) && tiles.get(j).getCol().size() <= thres) {
                            int newcoldep = Sets.union(curGroup.getCol(), tiles.get(j).getCol()).size();
							//int newcoldep = newColCount(curGroup, tiles.get(j));
							int overlap = Sets.intersection(curGroup.getCol(), tiles.get(j).getCol()).size();
                            //int overlap = getOverlapColCount(curGroup, tiles.get(j));
							int diff = newcoldep - overlap;
							if (newcoldep < thres && (!valueSet || overlap - diff > dist)) {
								index = j;
								dist = overlap - diff;
								valueSet = true;
							}
						}
					}
					if (valueSet) {
						curGroupIds.add(index);
						curGroup.addTile(tiles.get(index));
						added = true;
					}
				}

				// check if we have enough tiles
				if (curGroupIds.size() >= minsize || maxsize == curGroupIds.size()) {
					lastGroupList.add(curGroup);
					lastGroupIdList.add(curGroupIds);
                    for (Integer curGroupId : curGroupIds) {
                        used.add(curGroupId);
                    }
                    Config.logFile.log("S: " + thres + " @ " + curGroupIds.size());
				}
			}
		}
	}
}
