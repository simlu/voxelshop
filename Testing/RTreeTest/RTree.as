package 
{
	// wrapper class for the rtree

	import org.ffilmation.utils.rtree.*;

	public class RTree {

		// holds a list of all collision objects
		protected var colObjs:Array = [];
	
		// holds the collision objects for detection
		protected var rtree:fRTree = new fRTree();
	
		// constructor
		public function RTree()
		{
		}
	
		// get a certain object
		public function get(id:int):Array
		{
			return colObjs[id];
		}
	
		// returns list of ids that were hit, including the id itself
		public function getIntersecting(id:int):Array
		{
			return rtree.intersects(new fCube(colObjs[id][0], colObjs[id][1], 0, 
			colObjs[id][0] + colObjs[id][2], colObjs[id][1] + colObjs[id][3], 0));
		}
	
		// returns list of ids that were hit
		public function hitTest(x:int, y:int, w:int, h:int):Array
		{
			return rtree.intersects(new fCube(x, y, 0, x + w, y + h, 0));
		}
	
		// remove an element by id
		public function remove(id:int):Boolean
		{
			if (colObjs[id] == undefined)
			{
				// can not remove it
				return false;
			}
			else
			{
				if (rtree.deleteCube(new fCube(colObjs[id][0], colObjs[id][1], 0, 
				colObjs[id][0] + colObjs[id][2], colObjs[id][1] + colObjs[id][3], 0),id))
				{
					// object was removed form rtree
					colObjs[id] = undefined;
					// splice will reset the index (!) - bad
					//colObjs.splice(id,1);
				}
				else
				{
					// strange, we were not able to remove the object (?!)
					throw Error("Internal rtree error.");
				}
			}
			return true;
		}
	
		// add an element
		public function add(x:int, y:int, w:int, h:int, id:int)
		{
			if (colObjs[id] != undefined)
			{
				// extists already, so we have to remove it
				this.remove(id);
			}
			colObjs[id] = [x,y,w,h];
			rtree.addCube(new fCube(x, y, 0, x + w, y + h, 0), id);
		}
	
	}

}