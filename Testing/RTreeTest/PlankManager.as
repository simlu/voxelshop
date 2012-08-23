package  {
	import flash.display.MovieClip;
	import flash.display.Sprite;
	import flash.text.TextField;
	import flash.display.Bitmap;
	import flash.events.Event;
	import flash.display.Loader;
	import flash.net.URLRequest;
	import flash.display.LoaderInfo;
	
	// manages the planks
	
	public class PlankManager {
		
		// the view position
		protected var viewx:int = 0;
		protected var viewy:int = 0;
		
		// the view position the visibility was updated the last time
		protected var viewOldx:int = 0;
		protected var viewOldy:int = 0;
		
		// the size of the view window
		protected var viewPortx:int = 100;
		protected var viewPorty:int = 100;
		
		// buffer distance before we update visibility list again
		protected var VISIBLE_LIST_REFRESH_DIST:int = Math.pow(500,2);
		protected var VISIBLE_BUFFER:int = Math.ceil(Math.sqrt(VISIBLE_LIST_REFRESH_DIST));
		
		// holds all the planks, so it's easier to shift them
		protected var plankHolder:MovieClip = new MovieClip();
		// buffers all the tiles so we dont have to recreate them
		protected var buffer:Array = new Array();
		
		// rtree that holds all the planks.. so we know what should be visisble
		protected var rtree:RTree = new RTree();
		
		// draw our elements that are visible
		private function drawVisible() {
			var visList:Array = rtree.hitTest(-VISIBLE_BUFFER-viewx, -VISIBLE_BUFFER-viewy,
											  viewPortx+2*VISIBLE_BUFFER,  viewPorty+2*VISIBLE_BUFFER);
			
			var index:int;
			var tile:Tile;
			var i:int;
			for (i = 0; i < plankHolder.numChildren; i++) {
				tile = plankHolder.getChildAt(i) as Tile;
				index = visList.indexOf(tile._uid);
				if (index == -1) {
					// removes unnecessary plank
					tile.remove();
					i--;
				} else {
					// remove index (so we don't add a duplicate!)
					visList[index] = undefined;
					// here it doesn't matter that the index is reset, I think ...
					//visList.splice(index,1);
				}
			}
			var pos:Array;
			// add the missing planks
			for (i = 0; i < visList.length; i++) {
				if (visList[i] != undefined) {
					pos = rtree.get(visList[i]);
					plankHolder.addChild(buffer[visList[i]]);
					//plankHolder.addChild(new Plank(pos[0], pos[1], pos[2], pos[3], visList[i]));
				}
			}
		}

		// notify everyone that the view position has changed
		public function setViewPos(x:int, y:int) {
			// we are moving the whole container
			MovieClip(plankHolder.parent).x = -x;
			MovieClip(plankHolder.parent).y = -y;
			// this would move the individual objects instead
			// EventCenter.getInstance().dispatchEvent(new PositionEvent(PositionEvent.VIEW_POSITION_CHANGE, -x, -y));
			viewx = -x;
			viewy = -y;
			// check when the last refresh was
			if (Math.pow(viewOldx + x, 2) + Math.pow(viewOldy + y, 2) > VISIBLE_LIST_REFRESH_DIST) {
				viewOldx = -x;
				viewOldy = -y;
			}
		}
		
		public function addTile(bmpUrl: String, x:int, y:int, uid:int) {
			
			// add some tiles
			var loader:Loader = new Loader();
				loader.contentLoaderInfo.addEventListener(Event.COMPLETE, getComplete(x,y,uid));
				loader.load(new URLRequest(bmpUrl));
			
			function getComplete(x:int, y:int, uid:int):Function {
				return function onComplete (event:Event):void
				{
					//trace(x);
					var bmp:Bitmap = Bitmap(LoaderInfo(event.target).content);
					buffer[uid] = new Tile(x, y, bmp, uid);
					rtree.add(x, y, bmp.width, bmp.height, uid);
					drawVisible();
				}
			}
			
			
			//rtree.add(i*110-50, j*20-5, 100, 10, i*200 + j);
		}
		
		// constructor, takes the stage size
		public function PlankManager(viewPortx:int, viewPorty:int) {
			
			// r tree test
			/*
			rtree.add(10,20,5,5,1);
			rtree.add(10,15,5,5,2);
			rtree.add(10,17,5,5,3);
			rtree.remove(1);
			trace (rtree.get(3));
			*/
			
			// set the view port size
			this.viewPortx = viewPortx;
			this.viewPorty = viewPorty;
			// request that it is added to the timeline
			EventCenter.getInstance().dispatchEvent(new MovieClipEvent(MovieClipEvent.SHOW_REQUEST, plankHolder));
			
			// create a lot of random planks for testing
			/*
			for (var i:int = 0; i < 4000; i++) {
				// prevent overlaps
				do {
					var x:int = Math.round(Math.random()*10000)-5000;
					var y:int = Math.round(Math.random()*10000)-5000;
				} while (rtree.hitTest(-50+x,-5+y,100,10).length != 0);
				rtree.add(-50+x,-5+y,100,10,i);
				//plankHolder.addChild(new Plank(-50+x,-5+y,100,10, i));
			}
			//*/
			/*
			for (var i:int = 0; i < 200; i++) {
				for (var j:int = 0; j < 200; j++) {
					rtree.add(i*110-50, j*20-5, 100, 10, i*200 + j);
					//plankHolder.addChild(new Plank(i*110-50, j*20-5, 100, 10, i*200 + j));
				}
			}
			//*/
			drawVisible();
		}

	}
	
}
