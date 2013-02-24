package vitco.datastruct;

/*
* Logical representation of a tile.
 */

public class Tile {
	public int x,y,chunk,id;
	public Tile(int chunk, int x, int y, int id) {
		this.x = x;
		this.y = y;
		this.chunk = chunk;
		this.id = id;
	}
	
}
