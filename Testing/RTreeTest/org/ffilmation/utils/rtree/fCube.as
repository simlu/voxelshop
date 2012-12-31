// This is an actionscript port of the JSI Library.
// Credit goes to the original developers
// http://jsi.sourceforge.net/
package org.ffilmation.utils.rtree  {

	/**
	* <p>The fCube is the data type used in our rTree. fCubes can be inserted and searched for efficiently.</p>
	* This is an actionscript port of the JSI Library. Credit goes to the original developers.
	* @see http://en.wikipedia.org/wiki/Rtree
	* @see http://jsi.sourceforge.net/
	*
	*/
	public class fCube {
	  
	  /**
	  * Number of dimensions in a cube. In theory this
	  * could be exended to three or more dimensions.
	  */
	  public static const DIMENSIONS:int = 3
	  
	  /**
	  * Array containing the minimum value for each dimension; ie { min(x), min(y) }
	  * @private
	  */
	  public var max:Array
	  
	  /**
	  * Array containing the maximum value for each dimension; ie { max(x), max(y) }
	  * @private
	  */
	  public var min:Array
	
	  /**
	  * Constructor.
	  * 
	  * @param x1 coordinate of any corner of the cube
	  * @param y1 (see x1)
	  * @param z1 (see x1)
	  * @param x2 coordinate of the opposite corner
	  * @param y2 (see x2)
	  * @param z2 (see x2)
	  */
	  public function fCube(x1:Number, y1:Number, z1:Number, x2:Number, y2:Number, z2:Number):void {
	    this.min = new Array(fCube.DIMENSIONS)
	    this.max = new Array(fCube.DIMENSIONS)
	    setValues(x1, y1, z1, x2, y2, z2)
	  }
	
  
	  /**
	  * Sets the size of the cube.
	  * 
	  * @param x1 coordinate of any corner of the cube
	  * @param y1 (see x1)
	  * @param z1 (see x1)
	  * @param x2 coordinate of the opposite corner
	  * @param y2 (see x2)
	  * @param z2 (see x2)
		* @private
	  */
	  public function setValues(x1:Number, y1:Number, z1:Number, x2:Number, y2:Number, z2:Number):void {
	    this.min[0] = Math.min(x1, x2)
	    this.min[1] = Math.min(y1, y2)
	    this.min[2] = Math.min(z1, z2)
	    this.max[0] = Math.max(x1, x2)        
	    this.max[1] = Math.max(y1, y2) 
	    this.max[2] = Math.max(z1, z2) 
	  }
	  
	  /**
	  * Sets the size of the cube.
	  * 
	  * @param min array containing the minimum value for each dimension; ie { min(x), min(y) }
	  * @param max array containing the maximum value for each dimension; ie { max(x), max(y) }
		* @private
	  */
	  public function setArrays(min:Array, max:Array):void {
	  	for(var i:int=0;i<min.length;i++) this.min[i] = min[i]
	  	for(i=0;i<max.length;i++) this.max[i] = max[i]
	  }
	  
	  /**
	  * Make a copy of this cube
	  * 
	  * @return copy of this cube
	  */
	  public function copy():fCube {
	    return new fCube(this.min[0],this.min[1],this.min[2],this.max[0],this.max[1],this.max[2])
	  }
	  
	  /**
	  * Determine whether an edge of this cube overlies the equivalent 
	  * edge of the passed cube
		* @private
	  */
	  public function edgeOverlaps(r:fCube):Boolean {
	    for (var i:int=0; i<fCube.DIMENSIONS; i++) {
	      if (this.min[i] == r.min[i] || this.max[i] == r.max[i]) {
	        return true; 
	      } 
	    }  
	    return false;
	  }
	  
	  /**
	  * Determine whether this cube intersects the passed cube
	  * 
	  * @param r The cube that might intersect this cube
	  * 
	  * @return true if the rectangles intersect, false if they do not intersect
	  */
	  public function intersects(r:fCube):Boolean {
	    // Every dimension must intersect. If any dimension
	    // does not intersect, return false immediately.
	    for (var i:int=0; i<fCube.DIMENSIONS; i++) {
	      if (this.max[i] < r.min[i] || this.min[i] > r.max[i]) {
	        return false;
	      }
	    }
	    return true;
	  }
	 
	  /**
	  * Determine whether this cube contains the passed cube
	  * 
	  * @param r The cube that might be contained by this cube
	  * 
	  * @return true if this cube contains the passed cube, false if
	  *         it does not
	  */
	  public function contains(r:fCube):Boolean {
	    for (var i:int=0; i<fCube.DIMENSIONS; i++) {
	      if (this.max[i] < r.max[i] || this.min[i] > r.min[i]) {
	        return false;
	      }
	    }
	    return true;     
	  }
	 
	  /**
	  * Determine whether this cube is contained by the passed cube
	  * 
	  * @param r The cube that might contain this cube
	  * 
	  * @return true if the passed cube contains this cube, false if
	  *         it does not
	  */
	  public function containedBy(r:fCube):Boolean {
	    for (var i:int=0; i<fCube.DIMENSIONS; i++) {
	      if (this.max[i] > r.max[i] || this.min[i] < r.min[i]) {
	        return false;
	      }
	    }
	    return true;  
	  }
	  
	  /**
	  * Return the distance between this cube and the passed point.
	  * If the cube contains the point, the distance is zero.
	  * 
	  * @param p Point to find the distance to, as an array of coordinates [x,y,z]
	  * 
	  * @return distance beween this cube and the passed point.
	  */
	  public function distanceToPoint(p:Array):Number {
	    var distanceSquared:Number = 0;
	    for (var i:int=0; i<fCube.DIMENSIONS; i++) {
	      var greatestMin:Number = Math.max(min[i], p[i])
	      var leastMax:Number = Math.min(max[i], p[i])
	      if (greatestMin > leastMax) {
	        distanceSquared += ((greatestMin - leastMax) * (greatestMin - leastMax))
	      }
	    }
	    return Math.sqrt(distanceSquared)
	  }
	  
	  /**
	  * Return the distance between this cube and the passed cube.
	  * If the rectangles overlap, the distance is zero.
	  * 
	  * @param r fCube to find the distance to
	  * 
	  * @return distance between this cube and the passed cube
	  */
	  public function distanceToCube(r:fCube):Number {
	    var distanceSquared:Number = 0;
	    for (var i:int=0; i<fCube.DIMENSIONS; i++) {
	      var greatestMin:Number = Math.max(min[i], r.min[i])
	      var leastMax:Number = Math.min(max[i], r.max[i])
	      if (greatestMin > leastMax) {
	        distanceSquared += ((greatestMin - leastMax) * (greatestMin - leastMax))
	      }
	    }
	    return Math.sqrt(distanceSquared)
	  }
	   
	  /**
	  * Return the squared distance from this cube to the passed point
	  */
	  private function getDistanceSquared(dimension:int, point:Number):Number {
	    var distanceSquared:Number = 0
	    var tempDistance:Number = point - max[dimension]
	    for (var i:int=0; i<fCube.DIMENSIONS; i++) {
	      if (tempDistance > 0) {
	        distanceSquared = (tempDistance * tempDistance)
	        break;
	      } 
	      tempDistance = min[dimension] - point
	    }
	    return distanceSquared
	  }
	  
	  /**
	  * Return the furthest possible distance between this cube and
	  * the passed cube. 
	  * 
	  * Find the distance between this cube and each corner of the
	  * passed cube, and use the maximum.
		* @private
	  *
	  */
	  public function furthestDistance(r:fCube):Number {
	     var distanceSquared:Number = 0
	     for (var i:int=0; i<fCube.DIMENSIONS; i++) {
	       distanceSquared += Math.max(this.getDistanceSquared(i, r.min[i]), this.getDistanceSquared(i, r.max[i]))
	     }
	     return Math.sqrt(distanceSquared)
	  }
	  
	  /**
	  * Calculate the volume by which this cube would be enlarged if
	  * added to the passed cube. Neither cube is altered.
	  * 
	  * @param r fCube to union with this cube, in order to 
	  *          compute the difference in volume of the union and the
	  *          original cube
		* @private
	  */
	  public function enlargement(r:fCube):Number {
	    var enlargedArea:Number = 1
	    for (var i:int=0; i<fCube.DIMENSIONS; i++) {
	    	enlargedArea *= (Math.max(this.max[i], r.max[i]) - Math.min(this.min[i], r.min[i]))
	    }
	    return enlargedArea - this.volume()
	  }
	  
	  /**
	  * Compute the volume of this cube.
	  * 
	  * @return The volume of this cube
	  */
	  public function volume():Number {
	    var volume:Number = 1
	    for (var i:int=0; i<fCube.DIMENSIONS; i++) {
	  		volume *= (this.max[i] - this.min[i])
	  	}
	  	return volume
	  }
	  
	  /**
	  * Computes the union of this cube and the passed cube, storing
	  * the result in this cube.
	  * 
	  * @param r fCube to add to this cube
	  */
	  public function add(r:fCube):void {
	    for (var i:int=0; i<fCube.DIMENSIONS; i++) {
	      if (r.min[i] < this.min[i]) {
	        this.min[i] = r.min[i]
	      }
	      if (r.max[i] > this.max[i]) {
	        this.max[i] = r.max[i]
	      }
	    }
	  }
	  
	  /**
	  * Find the the union of this cube and the passed cube.
	  * Neither cube is altered
	  * 
	  * @param r The cube to union with this cube
	  */
	  public function union(r:fCube):fCube {
	    var union:fCube = this.copy()
	    union.add(r)
	    return union
	  }
	  
	  /**
	  * Determine whether this cube is equal to a given object.
	  * Equality is determined by the bounds of the cube.
	  * 
	  * @param o The object to compare with this cube
	  */
	  public function equals(o:Object):Boolean {

	    if (o is fCube) {
	      var r:fCube = o as fCube
				for (var i:int=0; i<fCube.DIMENSIONS; i++) {
	      	if (r.min[i] != this.min[i] || r.max[i] != this.max[i]) return false
	      }
	      return true
	    } else return false

	  }
	
	  /**
	  * Return a string representation of this cube, in the form: 
	  * (1.2, 3.4), (5.6, 7.8)
	  * 
	  * @return String String representation of this cube.
	  */
	  public function toString():String {
	    
	    var sb:String = ""
	    
	    // min coordinates
	    sb+="("
			for (var i:int=0; i<fCube.DIMENSIONS; i++) {
	      if (i > 0) {
	        sb+=", "
	      }
	      sb+=this.min[i]
	    } 
	    sb+="), ("
	    
	    // max coordinates
			for (i=0; i<fCube.DIMENSIONS; i++) {
	      if (i > 0) {
	        sb+=", "
	      }
	      sb+=this.max[i]
	    } 
	    sb+=")"
	    
	    return sb
	    
	  }

	}

}