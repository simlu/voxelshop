package 
{
	import flash.display.MovieClip;
	import flash.events.*;

	// displays a plank (an object of the world)

	public class Plank extends MovieClip
	{
		
		// if there was a change (so we need to redraw in next ondraw event!)
		protected var shiftChanged:Boolean = false;
		// how this object is shifted
		protected var shiftx:int = 0;
		protected var shifty:int = 0;
		
		// the position of this plank object
		protected var ox:int = 0;
		protected var oy:int = 0;
		protected var ow:int = 0;
		protected var oh:int = 0;
		
		// the id
		public var id:int;

		// constructor
		public function Plank(x:int, y:int, w:int, h:int, id:int)
		{
			this.id = id;
			ox = x;
			oy = y;
			ow = w;
			oh = h;
			// movie clip added
			this.addEventListener(Event.ADDED, beginClass);
			// on frame show
			this.addEventListener(Event.ENTER_FRAME, eFrameEvents);
		}
		
		// called when the movie clip is ready
		private function beginClass(e:Event):void{
			// register the on world view change event
			EventCenter.getInstance().addEventListener(PositionEvent.VIEW_POSITION_CHANGE, setShift);
			// paint
			this.graphics.beginFill(0x558855);
			this.graphics.drawRect(ox, oy, ow, oh);
			this.graphics.endFill();			
		}
		
		private function eFrameEvents(e:Event):void{
			// only change if view has changed
			if (shiftChanged) {
				MovieClip(this.parent).x = shiftx;
				MovieClip(this.parent).y = shifty;
				shiftChanged = false;
			}
		}
		
		// change to new position
		private function setShift(ev:PositionEvent) {
			shiftx = ev.x;
			shifty = ev.y;
			shiftChanged = true;
		}
		
		// destroys this element and removes it form the main timeline
		public function kill() {
			EventCenter.getInstance().removeEventListener(PositionEvent.VIEW_POSITION_CHANGE, setShift);
			this.removeEventListener(Event.ADDED, beginClass);
			this.removeEventListener(Event.ENTER_FRAME, eFrameEvents);
			MovieClip(this.parent).removeChild(this);
		}

	}

}