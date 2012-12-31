// This is an actionscript port of the JSI Library.
// Credit goes to the original developers
// http://jsi.sourceforge.net/
package org.ffilmation.utils.rtree  {

	import flash.geom.*
	import flash.utils.*

	/**
	* <p>An RTree is a data structure that organizes and indexes spatial information and provides efficient spatial search features.</p>
	* <p>This is an actionscript port of the JSI Library. Credit goes to the original developers.</p>
	* @see http://en.wikipedia.org/wiki/Rtree
	* @see http://jsi.sourceforge.net/
	*
	*/
	public class fRTree {
  
  	// Constants
  	////////////
  	
  	private static const DEFAULT_MAX_NODE_ENTRIES:int = 10
  	private static const DEFAULT_MIN_NODE_ENTRIES:int = 5

  	// Internal consistency checking - set to true if debugging tree corruption
  	private static const INTERNAL_CONSISTENCY_CHECKING:Boolean = false

  	// Used to mark the status of entries during a node split
  	private static const ENTRY_STATUS_ASSIGNED:int = 0
  	private static const ENTRY_STATUS_UNASSIGNED:int = 1

		// Properties
		/////////////

		/** @private */
  	public var maxNodeEntries:int
  	private var minNodeEntries:int
  	
  	// map of nodeId -> node object
  	private var nodeMap:Dictionary = new Dictionary()
  	
  	private var entryStatus:Array = null
  	private var initialEntryStatus:Array = null
  	
  	// stacks used to store nodeId and entry index of each node 
  	// from the root down to the leaf. Enables fast lookup
  	// of nodes when a split is propagated up the tree.
  	private var parents:Array = new Array
  	private var parentsEntry:Array = new Array
  	
  	// initialisation
  	private var treeHeight:int = 1 	// leaves are always level 1
  	private var rootNodeId:int = 0
  	private var size:int = 0
  	
  	// Enables creation of new nodes
  	private var highestUsedNodeId:int = rootNodeId
  	
  	// Deleted node objects are retained in the nodeMap, 
  	// so that they can be reused. Store the IDs of nodes
  	// which can be reused.
  	private var deletedNodeIds = new Array
  	
  	// List of nearest rectangles. Use a member variable to
  	// avoid recreating the object each time nearest() is called.
  	private var nearestIds = new Array
  	
  	// Public methods
  	/////////////////
  	
  	// Constructor
  	public function fRTree():void {  

  	  this.maxNodeEntries = fRTree.DEFAULT_MAX_NODE_ENTRIES
  	  this.minNodeEntries = fRTree.DEFAULT_MIN_NODE_ENTRIES
  	  
  	  // Obviously a node with less than 2 entries cannot be split.
  	  // The node splitting algorithm will work with only 2 entries
  	  // per node, but will be inefficient.
  	  if (this.maxNodeEntries < 2) { 
  	    trace("Invalid MaxNodeEntries = " + this.maxNodeEntries + " Resetting to default value of " + fRTree.DEFAULT_MAX_NODE_ENTRIES);
  	    this.maxNodeEntries = fRTree.DEFAULT_MAX_NODE_ENTRIES;
  	  }
  	  
  	  // The MinNodeEntries must be less than or equal to (int) (MaxNodeEntries / 2)
  	  if (this.minNodeEntries < 1 || this.minNodeEntries > this.maxNodeEntries / 2) {
  	    trace("MinNodeEntries must be between 1 and MaxNodeEntries / 2");
  	    this.minNodeEntries = this.maxNodeEntries / 2;
  	  }
  	  
  	  this.entryStatus = new Array(maxNodeEntries)
  	  this.initialEntryStatus = new Array(maxNodeEntries)
  	  
  	  for(var i:int=0;i<this.maxNodeEntries; i++) this.initialEntryStatus[i] = fRTree.ENTRY_STATUS_UNASSIGNED;
  	  
  	  var root:fRTreeNode = new fRTreeNode(rootNodeId, 1, maxNodeEntries);
  	  this.nodeMap[rootNodeId] = root
  	  
  	  //trace("init() " + " MaxNodeEntries = " + maxNodeEntries + ", MinNodeEntries = " + minNodeEntries);
  	}
  	
    /**
    * Adds a new cube to the spatial index
    * 
    * @param r  The cube to add to the spatial index.
    * @param id The ID of the cube to add to the spatial index.
    *           The result of adding more than one cube with
    *           the same ID is undefined.
    */ 
  	public function addCube(r:fCube,id:int,level:int=1):void {
  	  
  	  // I1 [Find position for new record] Invoke ChooseLeaf to select a 
  	  // leaf node L in which to place r
  	  var n:fRTreeNode = this.chooseNode(r,level)
  	  var newLeaf:fRTreeNode = null
  	  
  	  // I2 [Add record to leaf node] If L has room for another entry, 
  	  // install E. Otherwise invoke SplitNode to obtain L and LL containing
  	  // E and all the old entries of L
  	  if (n.entryCount < this.maxNodeEntries) {
  	    n.addEntryNoCopy(r,id)
  	  } else {
  	    newLeaf = this.splitNode(n,r,id)
  	  }
  	  
  	  // I3 [Propagate changes upwards] Invoke AdjustTree on L, also passing LL
  	  // if a split was performed
  	  var newNode:fRTreeNode = this.adjustTree(n, newLeaf)
  	
  	  // I4 [Grow tree taller] If node split propagation caused the root to 
  	  // split, create a new root whose children are the two resulting nodes.
  	  if (newNode != null) {
  	    var oldRootNodeId:int = this.rootNodeId
  	    var oldRoot:fRTreeNode = this.getNode(oldRootNodeId)
  	    
  	    this.rootNodeId = getNextNodeId()
  	    treeHeight++
  	    var root:fRTreeNode = new fRTreeNode(rootNodeId, treeHeight, maxNodeEntries)
  	    root.addEntry(newNode.mbr, newNode.nodeId)
  	    root.addEntry(oldRoot.mbr, oldRoot.nodeId)
  	    nodeMap[rootNodeId] = root
  	  }
  	  
  	  if (fRTree.INTERNAL_CONSISTENCY_CHECKING) this.checkConsistency(rootNodeId, treeHeight, null)

  	} 
  	
    /**
    * Deletes a cube from the spatial index
    * 
    * @param r  The cube to delete from the spatial index
    * @param id The ID of the cube to delete from the spatial
    *           index
    * 
    * @return true  if the cube was deleted
    *         false if the cube was not found, or the 
    *               cube was found but with a different ID
    */
  	public function deleteCube(r:fCube, id:int):Boolean {
  	  
  	  // FindLeaf algorithm inlined here. Note the "official" algorithm 
  	  // searches all overlapping entries. This seems inefficient to me, 
  	  // as an entry is only worth searching if it contains (NOT overlaps)
  	  // the cube we are searching for.
  	  //
  	  // Also the algorithm has been changed so that it is not recursive.
  	  
  	  // FL1 [Search subtrees] If root is not a leaf, check each entry 
  	  // to determine if it contains r. For each entry found, invoke
  	  // findLeaf on the node pointed to by the entry, until r is found or
  	  // all entries have been checked
  		this.parents = []
  		this.parents.push(rootNodeId)
  		
  		this.parentsEntry = []  
  		this.parentsEntry.push(-1)
  		var n:fRTreeNode = null
  		var foundIndex:int = -1;  // index of entry to be deleted in leaf
  		
  		while (foundIndex == -1 && this.parents.length > 0) {
  		  n = this.getNode(this.parents[this.parents.length-1])
  		  var startIndex:int = this.parentsEntry[this.parentsEntry.length-1] + 1
  	    
  	    if (!n.isLeaf()) {
  	      //trace("searching node " + n.nodeId + ", from index " + startIndex)
		  	  var contains:Boolean = false
  	      for (var i:int=startIndex;i<n.entryCount; i++) {
		  	    if(n.entries[i].contains(r)) {
		  	      this.parents.push(n.ids[i])
		  	      this.parentsEntry.pop()
		  	      this.parentsEntry.push(i) 		// this becomes the start index when the child has been searched
		  	      this.parentsEntry.push(-1)
		  	      contains = true
  	          break; // ie go to next iteration of while()
		  	    }
		  	  }
  	      if (contains) continue
  	    } else {
  	      foundIndex = n.findEntry(r, id)
  	    }
  	    
  	    this.parents.pop()
  	    this.parentsEntry.pop()
  		} // while not found
  		
  		if(foundIndex != -1) {
  		  n.deleteEntry(foundIndex, minNodeEntries)
  	    this.condenseTree(n)
  	    this.size--
  		}
  		
  	  // shrink the tree if possible (i.e. if root node has exactly one entry,and that 
  	  // entry is not a leaf node, delete the root (it's entry becomes the new root)
  	  var root:fRTreeNode = this.getNode(rootNodeId)
  	  while(root.entryCount == 1 && treeHeight > 1) {
  	      root.entryCount = 0
  	      this.rootNodeId = root.ids[0]
  	      this.treeHeight--
  	      root = getNode(rootNodeId)
  	  }
  	  
  	  return (foundIndex != -1)
  	  
  	}
  	
   	/**
   	* Finds all rectangles that are nearest to the passed cube
   	* 
   	* @param p The point for which this method finds the nearest neighbours, as an Array of coordinates [x,y,z]
   	* 
   	* @param distance The furthest distance away from the cube to search. Rectangles further than this will not be found.
   	* This should be as small as possible to minimise the search time.
   	* Dafault Number.POSITIVE_INFINITY value guarantees that the nearest cube is found, no matter how far away, although this will slow down the algorithm.
   	*
   	* @return an Array with the found rectangles
   	*/
  	public function nearest(p:Array,furthestDistance:Number=Number.POSITIVE_INFINITY):Array {
  	  
  	  var rootNode:fRTreeNode = getNode(rootNodeId)
  	 
  	  // This stores its results into the nearestIds Array
  	  this.recursiveNearest(p, rootNode, furthestDistance)
  	 
  	  var ret:Array = this.nearestIds
  	  this.nearestIds = []
  	  return ret
  	}
  	 
    /**
    * Finds all rectangles that intersect the passed cube.
    * 
    * @param  r The cube for which this method finds intersecting rectangles.
    *
    * @return An array with all cube Ids than intersect the input cube
    */
	  public function intersects(r:fCube):Array {
  	  
  	  var rootNode:fRTreeNode = getNode(rootNodeId)
  	  
  	  var found:Array = []
  	  this.recursiveIntersects(found, r, rootNode)
  	  return found
  	  
  	}
  	
    /**
    * Finds all rectangles contained by the passed cube.
    * 
    * @param r The cube for which this method finds contained rectangles.
    * 
    * @return An array with all cube Ids than intersect the input cube
    */
  	public function contains(r:fCube):Array {
  	  
  	  var ret:Array = []
  	  
  	  // find all rectangles in the tree that are contained by the passed cube
  	  // written to be non-recursive (should model other searches on this?)
  	  this.parents = []
  	  this.parents.push(this.rootNodeId)
  	  
  	  this.parentsEntry = []
  	  this.parentsEntry.push(-1)
  	  
  	  // TODO: possible shortcut here - could test for intersection with the 
  	  // MBR of the root node. If no intersection, return immediately.
  	  
  	  while(this.parents.length > 0) {
  	    var n:fRTreeNode = this.getNode(this.parents[this.parents.length-1])
  	    var startIndex:int = this.parentsEntry[this.parentsEntry.length-1] + 1
  	    
  	    if (!n.isLeaf()) {
  	      // go through every entry in the index node to check
  	      // if it intersects the passed cube. If so, it 
  	      // could contain entries that are contained.
  	      var intersects:Boolean = false
  	      for (var i:int = startIndex; i<n.entryCount;i++) {
  	        if(r.intersects(n.entries[i])) {
  	          this.parents.push(n.ids[i])
  	          this.parentsEntry.pop()
  	          this.parentsEntry.push(i) 			// this becomes the start index when the child has been searched
  	          this.parentsEntry.push(-1)
  	          intersects = true
  	          break; // ie go to next iteration of while()
  	        }
  	      }
  	      if (intersects) continue
  	    } else {
  	      // go through every entry in the leaf to check if 
  	      // it is contained by the passed cube
  	      for (i=0; i<n.entryCount; i++) {
  	        if (r.contains(n.entries[i])) {
  	          ret[ret.length] = n.ids[i]
  	        } 
  	      }                       
  	    }
  	    this.parents.pop()
  	    this.parentsEntry.pop() 
  	  }
  	  
  	  return ret
  	  
  	}
  	
 	  /**
    * Returns the bounds of all the entries in the spatial index,
    * or null if there are no entries.
    */
  	public function getBounds():fCube {
  	  
  	  var bounds:fCube = null
  	  
  	  var n:fRTreeNode = this.getNode(this.rootNodeId)
  	  if (n != null && n.mbr != null) {
  	    bounds = n.mbr.copy()
  	  }
  	  return bounds
  	}
  	  
		// Private methods
		//////////////////

  	// Get the next available node ID. Reuse deleted node IDs if possible
  	private function getNextNodeId():int {
  	  var nextNodeId:int = 0
  	  if(this.deletedNodeIds.length>0) {
  	    nextNodeId = deletedNodeIds.pop()
  	  } else {
  	    nextNodeId = 1 + this.highestUsedNodeId++
  	  }
  	  return nextNodeId
  	}
  	
  	// Get a node object, given the ID of the node.
  	private function getNode(index:int):fRTreeNode {
  	  return nodeMap[index]
  	}
  	
  	/**
  	* Split a node. Algorithm is taken pretty much verbatim from
  	* Guttman's original paper.
  	* 
  	* @return new node object.
  	*/
  	private function splitNode(n:fRTreeNode, newRect:fCube, newId:int):fRTreeNode {
  	  
  	  // [Pick first entry for each group] Apply algorithm pickSeeds to 
  	  // choose two entries to be the first elements of the groups. Assign
  	  // each to a group.
  	  
  	  for(var i:int=0;i<this.maxNodeEntries;i++) this.entryStatus[i] = this.initialEntryStatus[i]
  	  
  	  var newNode:fRTreeNode = null
  	  newNode = new fRTreeNode(getNextNodeId(), n.level, this.maxNodeEntries)
  	  this.nodeMap[newNode.nodeId] = newNode
  	  
  	  this.pickSeeds(n, newRect, newId, newNode) 	// this also sets the entryCount to 1
  	  
  	  // [Check if done] If all entries have been assigned, stop. If one
  	  // group has so few entries that all the rest must be assigned to it in 
  	  // order for it to have the minimum number m, assign them and stop. 
  	  while (n.entryCount + newNode.entryCount < this.maxNodeEntries + 1) {
  	    if (this.maxNodeEntries + 1 - newNode.entryCount == this.minNodeEntries) {
  	      // assign all remaining entries to original node
  	      for(i=0; i < this.maxNodeEntries; i++) {
  	        if (this.entryStatus[i] == fRTree.ENTRY_STATUS_UNASSIGNED) {
  	          this.entryStatus[i] = fRTree.ENTRY_STATUS_ASSIGNED
  	          n.mbr.add(n.entries[i])
  	          n.entryCount++
  	        }
  	      }
  	      break;
  	    }   
  	    if (this.maxNodeEntries + 1 - n.entryCount == this.minNodeEntries) {
  	      // assign all remaining entries to new node
  	      for(i=0;i<this.maxNodeEntries; i++) {
  	        if(this.entryStatus[i] == fRTree.ENTRY_STATUS_UNASSIGNED) {
  	          this.entryStatus[i] = fRTree.ENTRY_STATUS_ASSIGNED
  	          newNode.addEntryNoCopy(n.entries[i], n.ids[i])
  	          n.entries[i] = null
  	        }
  	      }
  	      break;
  	    }
  	    
  	    // [Select entry to assign] Invoke algorithm pickNext to choose the
  	    // next entry to assign. Add it to the group whose covering cube 
  	    // will have to be enlarged least to accommodate it. Resolve ties
  	    // by adding the entry to the group with smaller volume, then to the 
  	    // the one with fewer entries, then to either. Repeat from S2
  	    this.pickNext(n, newNode)
  	  }
  	    
  	  n.reorganize(this)
  	  
  	  // check that the MBR stored for each node is correct.
  	  if (fRTree.INTERNAL_CONSISTENCY_CHECKING) {
  	    if (!n.mbr.equals(this.calculateMBR(n))) trace("Error: splitNode old node MBR wrong")
  	    if (!newNode.mbr.equals(this.calculateMBR(newNode))) trace("Error: splitNode new node MBR wrong")
  	  }
  	    
  	  return newNode
  	}
  	
  	/**
  	* Pick the seeds used to split a node.
  	* Select two entries to be the first elements of the groups
  	*/
  	private function pickSeeds(n:fRTreeNode, newRect:fCube, newId:int, newNode:fRTreeNode):void {
  	  
  	  // Find extreme rectangles along all dimension. Along each dimension,
		  // find the entry whose cube has the highest low side, and the one 
		  // with the lowest high side. Record the separation.
		  var maxNormalizedSeparation:Number = 0
		  var highestLowIndex:int = 0
		  var lowestHighIndex:int = 0
		  
		  // for the purposes of picking seeds, take the MBR of the node to include
		  // the new cube as well.
		  n.mbr.add(newRect)
		  
		  for (var d:int = 0; d < fCube.DIMENSIONS; d++) {
		    var tempHighestLow:Number = newRect.min[d]
		    var tempHighestLowIndex:int = -1 // -1 indicates the new cube is the seed
		    
		    var tempLowestHigh:Number = newRect.max[d]
		    var tempLowestHighIndex:int = -1;
		    
		    for (var i:int = 0; i < n.entryCount; i++) {
		      var tempLow:Number = n.entries[i].min[d]
		      if (tempLow >= tempHighestLow) {
		         tempHighestLow = tempLow
		         tempHighestLowIndex = i
		      } else {  // ensure that the same index cannot be both lowestHigh and highestLow
		        var tempHigh:Number = n.entries[i].max[d]
		        if (tempHigh <= tempLowestHigh) {
		          tempLowestHigh = tempHigh
		          tempLowestHighIndex = i
		        }
		      }
		    
		      // PS2 [Adjust for shape of the cube cluster] Normalize the separations
		      // by dividing by the widths of the entire set along the corresponding
		      // dimension
		      var normalizedSeparation:Number = (tempHighestLow - tempLowestHigh) / (n.mbr.max[d] - n.mbr.min[d])
		      
		      // PS3 [Select the most extreme pair] Choose the pair with the greatest
		      // normalized separation along any dimension.
		      if (normalizedSeparation > maxNormalizedSeparation) {
		        maxNormalizedSeparation = normalizedSeparation
		        highestLowIndex = tempHighestLowIndex
		        lowestHighIndex = tempLowestHighIndex
		      }
		    }
		  }
		    
		  // highestLowIndex is the seed for the new node.
		  if (highestLowIndex == -1) {
		    newNode.addEntry(newRect, newId)
		  } else {
		    newNode.addEntryNoCopy(n.entries[highestLowIndex], n.ids[highestLowIndex])
		    n.entries[highestLowIndex] = null
		    
		    // move the new cube into the space vacated by the seed for the new node
		    n.entries[highestLowIndex] = newRect
		    n.ids[highestLowIndex] = newId
		  }
		  
		  // lowestHighIndex is the seed for the original node. 
		  if (lowestHighIndex == -1) {
		    lowestHighIndex = highestLowIndex
		  }
		  
		  entryStatus[lowestHighIndex] = fRTree.ENTRY_STATUS_ASSIGNED
		  n.entryCount = 1
		  n.mbr.setArrays(n.entries[lowestHighIndex].min, n.entries[lowestHighIndex].max)
		
		}
	
	  /** 
	  * Pick the next entry to be assigned to a group during a node split.
	  * 
	  * [Determine cost of putting each entry in each group] For each 
	  * entry not yet in a group, calculate the volume increase required
	  * in the covering rectangles of each group  
	  */
	  private function pickNext(n:fRTreeNode, newNode:fRTreeNode):int {
	    var maxDifference:Number = Number.NEGATIVE_INFINITY
	    var next:int = 0
	    var nextGroup:int = 0
	    
	    for (var i:int = 0; i < this.maxNodeEntries; i++) {
	      if (this.entryStatus[i] == fRTree.ENTRY_STATUS_UNASSIGNED) {
	        
	        if (n.entries[i] == null) {
	          trace("Error: fRTreeNode " + n.nodeId + ", entry " + i + " is null");
	        }
	        
	        var nIncrease:Number = n.mbr.enlargement(n.entries[i])
	        var newNodeIncrease:Number = newNode.mbr.enlargement(n.entries[i])
	        var difference:Number = Math.abs(nIncrease - newNodeIncrease)
	         
	        if (difference > maxDifference) {
	          next = i;
	          
	          if (nIncrease < newNodeIncrease) {
	            nextGroup = 0; 
	          } else if (newNodeIncrease < nIncrease) {
	            nextGroup = 1;
	          } else if (n.mbr.volume() < newNode.mbr.volume()) {
	            nextGroup = 0;
	          } else if (newNode.mbr.volume() < n.mbr.volume()) {
	            nextGroup = 1;
	          } else if (newNode.entryCount < maxNodeEntries / 2) {
	            nextGroup = 0;
	          } else {
	            nextGroup = 1;
	          }
	          maxDifference = difference; 
	        }
	      }
	    }
	    
	    this.entryStatus[next] = fRTree.ENTRY_STATUS_ASSIGNED
	      
	    if(nextGroup == 0) {
	      n.mbr.add(n.entries[next])
	      n.entryCount++
	    } else {
	      // move to new node.
	      newNode.addEntryNoCopy(n.entries[next], n.ids[next])
	      n.entries[next] = null
	    }
	    
	    return next
	  }
	
	  /**
	  * Recursively searches the tree for the nearest entry. 
	  * nearest() must store the entry Ids as it searches the tree,
	  * in case a nearer entry is found.
	  * Uses the member variable nearestIds to store the nearest
	  * entry IDs.
	  * 
	  * [x] TODO rewrite this to be non-recursive?
	  */
	  private function recursiveNearest(p:Array, n:fRTreeNode, nearestDistance:Number):Number {
	    for (var i:int = 0; i < n.entryCount; i++) {
	      var tempDistance:Number = n.entries[i].distance(p)
	      if (n.isLeaf()) { // for leaves, the distance is an actual nearest distance 
	        if (tempDistance < nearestDistance) {
	          nearestDistance = tempDistance
	          this.nearestIds = []
	        }
	        if (tempDistance <= nearestDistance) {
	          this.nearestIds[this.nearestIds.length] = n.ids[i]
	        }     
	      } else { // for index nodes, only go into them if they potentially could have
	               // a cube nearer than actualNearest
	         if (tempDistance <= nearestDistance) {
	           // search the child node
	           nearestDistance = this.recursiveNearest(p, this.getNode(n.ids[i]), nearestDistance)
	         }
	      }
	    }
	    return nearestDistance
	  }
	  
	  /** 
	  * Recursively searches the tree for all intersecting entries.
	  * 
	  * [x] TODO rewrite this to be non-recursive? Make sure it
	  * doesn't slow it down.
	  */
	  private function recursiveIntersects(destiny:Array, r:fCube, n:fRTreeNode):void {
	    for (var i:int = 0; i < n.entryCount; i++) {
	      if (r.intersects(n.entries[i])) {
	        if (n.isLeaf()) {
	          destiny[destiny.length] = n.ids[i]
	        } else {
	          var childNode:fRTreeNode = this.getNode(n.ids[i])
	          this.recursiveIntersects(destiny,r,childNode)
	        }
	      }
	    }
	  }
	
	  /**
	  * Used by delete(). Ensures that all nodes from the passed node
	  * up to the root have the minimum number of entries.
	  * 
	  * Note that the parent and parentEntry stacks are expected to
	  * contain the nodeIds of all this.parents up to the root.
	  */
	  private var oldRectangle:fCube = new fCube(0, 0, 0, 0, 0, 0)
	  
	  private function condenseTree(l:fRTreeNode):void {
	    // CT1 [Initialize] Set n=l. Set the list of eliminated
	    // nodes to be empty.
	    var n:fRTreeNode = l
	    var parent:fRTreeNode = null
	    var parentEntry:int = 0
	    
	    var eliminatedNodeIds:Array = []
	  
	    // CT2 [Find parent entry] If N is the root, go to CT6. Otherwise 
	    // let P be the parent of N, and let En be N's entry in P  
	    while (n.level != treeHeight) {
	      parent = this.getNode(this.parents.pop())
	      parentEntry = this.parentsEntry.pop()
	      
	      // CT3 [Eliminiate under-full node] If N has too few entries,
	      // delete En from P and add N to the list of eliminated nodes
	      if (n.entryCount < minNodeEntries) {
	        parent.deleteEntry(parentEntry, minNodeEntries)
	        eliminatedNodeIds.push(n.nodeId)
	      } else {
	        // CT4 [Adjust covering cube] If N has not been eliminated,
	        // adjust EnI to tightly contain all entries in N
	        if (!n.mbr.equals(parent.entries[parentEntry])) {
	          oldRectangle.setArrays(parent.entries[parentEntry].min, parent.entries[parentEntry].max)
	          parent.entries[parentEntry].setArrays(n.mbr.min, n.mbr.max)
	          parent.recalculateMBR(oldRectangle)
	        }
	      }
	      // CT5 [Move up one level in tree] Set N=P and repeat from CT2
	      n = parent
	    }
	    
	    // CT6 [Reinsert orphaned entries] Reinsert all entries of nodes in set Q.
	    // Entries from eliminated leaf nodes are reinserted in tree leaves as in 
	    // Insert(), but entries from higher level nodes must be placed higher in 
	    // the tree, so that leaves of their dependent subtrees will be on the same
	    // level as leaves of the main tree
	    while (eliminatedNodeIds.length > 0) {
	      var e:fRTreeNode = this.getNode(eliminatedNodeIds.pop())
	      for (var j:int = 0; j < e.entryCount; j++) {
	        this.addCube(e.entries[j], e.ids[j], e.level)
	        e.entries[j] = null
	      }
	      e.entryCount = 0
	      this.deletedNodeIds.push(e.nodeId)
	    }
	  }
	
	  /**
	  *  Used by add(). Chooses a leaf to add the cube to.
	  */
	  private function chooseNode(r:fCube, level:int):fRTreeNode {
	    // CL1 [Initialize] Set N to be the root node
	    var n:fRTreeNode = this.getNode(rootNodeId)
	    this.parents = []
	    this.parentsEntry = []
	     
	    // CL2 [Leaf check] If N is a leaf, return N
	    while (true) {
	      if (n == null) {
	        trace("Could not get root node (" + rootNodeId + ")");  
	      }
	   
	      if (n.level == level) {
	        return n
	      }
	      
	      // CL3 [Choose subtree] If N is not at the desired level, let F be the entry in N 
	      // whose cube FI needs least enlargement to include EI. Resolve
	      // ties by choosing the entry with the cube of smaller volume.
	      var leastEnlargement:Number = n.getEntry(0).enlargement(r)
	      var index:int = 0 // index of cube in subtree
	      for (var i:int = 1; i < n.entryCount; i++) {
	        var tempRectangle:fCube = n.getEntry(i)
	        var tempEnlargement:Number = tempRectangle.enlargement(r)
	        if ((tempEnlargement < leastEnlargement) ||
	            ((tempEnlargement == leastEnlargement) && 
	             (tempRectangle.volume() < n.getEntry(index).volume()))) {
	          index = i
	          leastEnlargement = tempEnlargement
	        }
	      }
	      
	      this.parents.push(n.nodeId)
	      this.parentsEntry.push(index)
	    
	      // CL4 [Descend until a leaf is reached] Set N to be the child node 
	      // pointed to by Fp and repeat from CL2
	      n = this.getNode(n.ids[index])
	    }
	    
	    return null
	  }
	  
	  /**
	  * Ascend from a leaf node L to the root, adjusting covering rectangles and
	  * propagating node splits as necessary.
	  */
	  private function adjustTree(n:fRTreeNode, nn:fRTreeNode):fRTreeNode {
	    // AT1 [Initialize] Set N=L. If L was split previously, set NN to be 
	    // the resulting second node.
	    
	    // AT2 [Check if done] If N is the root, stop
	    while (n.level != this.treeHeight) {
	    
	      // AT3 [Adjust covering cube in parent entry] Let P be the parent 
	      // node of N, and let En be N's entry in P. Adjust EnI so that it tightly
	      // encloses all entry rectangles in N.
	      var parent:fRTreeNode = this.getNode(this.parents.pop())
	      var entry:int = this.parentsEntry.pop()
	      
	      if (parent.ids[entry] != n.nodeId) {
	        trace("Error: entry " + entry + " in node " + 
	             parent.nodeId + " should point to node " + 
	             n.nodeId + "; actually points to node " + parent.ids[entry])
	      }
	      
	      if (!parent.entries[entry].equals(n.mbr)) {
	        parent.entries[entry].setArrays(n.mbr.min, n.mbr.max)
	        parent.mbr.setArrays(parent.entries[0].min, parent.entries[0].max)
	        for (var i:int = 1; i < parent.entryCount; i++) {
	          parent.mbr.add(parent.entries[i])
	        }
	      }
	      
	      // AT4 [Propagate node split upward] If N has a partner NN resulting from 
	      // an earlier split, create a new entry Enn with Ennp pointing to NN and 
	      // Enni enclosing all rectangles in NN. Add Enn to P if there is room. 
	      // Otherwise, invoke splitNode to produce P and PP containing Enn and
	      // all P's old entries.
	      var newNode:fRTreeNode  = null
	      if (nn != null) {
	        if (parent.entryCount < maxNodeEntries) {
	          parent.addEntry(nn.mbr, nn.nodeId)
	        } else {
	          newNode = this.splitNode(parent, nn.mbr.copy(), nn.nodeId)
	        }
	      }
	      
	      // AT5 [Move up to next level] Set N = P and set NN = PP if a split 
	      // occurred. Repeat from AT2
	      n = parent
	      nn = newNode
	      
	      parent = null
	      newNode = null
	    }
	    
	    return nn
	  }
	  
	  /**
	  * Check the consistency of the tree.
	  */
	  private function checkConsistency(nodeId:int, expectedLevel:int, expectedMBR:fCube):void {
	    // go through the tree, and check that the internal data structures of 
	    // the tree are not corrupted.    
	    var n:fRTreeNode = this.getNode(nodeId)
	    
	    if (n == null) {
	      trace("Error: Could not read node " + nodeId);
	    }
	    
	    if (n.level != expectedLevel) {
	      trace("Error: fRTreeNode " + nodeId + ", expected level " + expectedLevel + ", actual level " + n.level);
	    }
	    
	    var calculatedMBR:fCube = calculateMBR(n)
	    
	    if (!n.mbr.equals(calculatedMBR)) {
	      trace("Error: fRTreeNode " + nodeId + ", calculated MBR does not equal stored MBR");
	    }
	    
	    if (expectedMBR != null && !n.mbr.equals(expectedMBR)) {
	      trace("Error: fRTreeNode " + nodeId + ", expected MBR (from parent) does not equal stored MBR");
	    }
	    
	    // Check for corruption where a parent entry is the same object as the child MBR
	    if (expectedMBR != null && n.mbr==(expectedMBR)) {
	      trace("Error: fRTreeNode " + nodeId + " MBR using same cube object as parent's entry");
	    }
	    
	    for (var i:int = 0; i < n.entryCount; i++) {
	      if (n.entries[i] == null) {
	        trace("Error: fRTreeNode " + nodeId + ", Entry " + i + " is null");
	      }     
	      
	      if (n.level > 1) { // if not a leaf
	        this.checkConsistency(n.ids[i], n.level - 1, n.entries[i])
	      }   
	    } 
	  }
	  
	  /**
	  * Given a node object, calculate the node MBR from it's entries.
	  * Used in consistency checking
	  */
	  private function calculateMBR(n:fRTreeNode):fCube {
	  	var rect:fCube = n.entries[0]
	    var mbr:fCube = new fCube(rect.min[0], rect.min[1], rect.min[2], rect.max[0], rect.max[1], rect.max[2])
	    
	    for (var i:int = 1; i < n.entryCount; i++) {
	      mbr.add(n.entries[i])
	    }
	    return mbr
	  }
	  
	}
	
}
