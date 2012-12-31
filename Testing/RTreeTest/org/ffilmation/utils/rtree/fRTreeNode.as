// This is an actionscript port of the JSI Library.
// Credit goes to the original developers
// http://jsi.sourceforge.net/
package org.ffilmation.utils.rtree  {

	/**
	* This is an actionscript port of the JSI Library. Credit goes to the original developers.
	* @see http://jsi.sourceforge.net/
	*
	* @private
	*/
	public class fRTreeNode {
  	
  	// Properties
  	public var nodeId:int = 0
  	public var mbr:fCube = null
  	public var entries:Array = null
  	public var ids:Array = null
  	public var level:int
  	public var entryCount:int = 0
  	
  	// Constructor
  	public function fRTreeNode(nodeId:int, level:int, maxNodeEntries:int):void {
  	  this.nodeId = nodeId
  	  this.level = level
  	  this.entries = []
  	  this.ids = []
  	}
  	 
  	// Methods
  	//////////
  	
  	// Add entry to the node
  	public function addEntry(r:fCube, id:int):void {
  	  this.ids[this.entryCount] = id
  	  this.entries[this.entryCount] = r.copy()
  	  this.entryCount++;
  	  if(this.mbr == null) {
  	    this.mbr = r.copy()
  	  } else {
  	    this.mbr.add(r)
  	  }
  	}
  	
  	// Add entry to the node
  	public function addEntryNoCopy(r:fCube, id:int):void {
  	  this.ids[this.entryCount] = id
  	  this.entries[this.entryCount] = r
  	  this.entryCount++;
  	  if(this.mbr == null) {
  	    this.mbr = r.copy()
  	  } else {
  	    this.mbr.add(r)
  	  }
  	}


  	// Return the index of the found entry, or -1 if not found
  	public function findEntry(r:fCube,id:int):int {
  		var ec:int = this.entryCount
  	  for (var i:int=0;i<ec;i++) {
  	  	if (id==this.ids[i] && r.equals(this.entries[i])) {
  	  	  return i
  	  	}
  	  }
  	  return -1;
  	}
  	
  	// delete entry. This is done by setting it to null and copying the last entry into its space.
  	public function deleteEntry(i:int,minNodeEntries:int):void {
		  var lastIndex:int = this.entryCount-1
		  var deletedRectangle:fCube = this.entries[i]
  	  this.entries[i] = null
		  if(i!=lastIndex) {
		  	this.entries[i] = this.entries[lastIndex]
		  	this.ids[i] = this.ids[lastIndex]
  	    this.entries[lastIndex] = null
		  }
  	  this.entryCount--
  	  
  	  // if there are at least minNodeEntries, adjust the MBR.
  	  // otherwise, don't bother, as the node will be 
  	  // eliminated anyway.
  	  if(this.entryCount>=minNodeEntries) this.recalculateMBR(deletedRectangle)
  	} 
  	
  	// oldRectangle is a cube that has just been deleted or made smaller.
  	// Thus, the MBR is only recalculated if the OldRectangle influenced the old MBR
  	public function recalculateMBR(deletedRectangle:fCube):void {
  	  
  	  if(this.mbr.edgeOverlaps(deletedRectangle)) { 
  	  	
  	  	var n:fCube = this.entries[0]
  	  	this.mbr.setArrays(n.min,n.max)
  	    
  	    var ec:int = this.entryCount
  	    for(var i:int=1;i<ec;i++) this.mbr.add(this.entries[i])

  	  }
  	}
  	 
  	public function getEntry(index:int):fCube {
  	  if(index<this.entryCount) return this.entries[index]
  	  return null
  	}
  	
  	public function getId(index:int):int {
  	  if(index<this.entryCount) return this.ids[index]
  	  return -1
  	}
  	
  	// Eliminate null entries, move all entries to the start of the source node
  	public function reorganize(rtree:fRTree):void {
  		
  	  var countdownIndex:int = rtree.maxNodeEntries-1 
  	  var ec:int = this.entryCount
  	  for(var index:int=0;index<ec; index++) {
  	    if(this.entries[index] == null) {
  	       while(this.entries[countdownIndex]==null && countdownIndex>index) countdownIndex--
  	       this.entries[index] = this.entries[countdownIndex]
  	       this.ids[index] = this.ids[countdownIndex]
  	       this.entries[countdownIndex] = null
  	    }
  	  }
  	  
  	}
  	
  	public function isLeaf():Boolean {
  	  return (this.level == 1)
  	}
 	
  
	}  
  

}
