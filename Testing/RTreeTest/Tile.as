package  {
	
	import flash.display.*;
	import flash.events.*;

    public class Tile extends MovieClip
    {
        public var _uid:int;
        public var _x:int;
        public var _y:int;
		public var _w:int;
		public var _h:int;
        public var _bmp:Bitmap;

        public function Tile(x:int, y:int, bmp:Bitmap, uid:int)
        {
            _uid = uid;
			_bmp = bmp;
			_x = x;
			_y = y;
			_w = bmp.width;
			_h = bmp.height;
			this.addEventListener(Event.ADDED, beginClass);
        }
		
		// called when the movie clip is ready
		private function beginClass(e:Event):void{
			this.addChild(_bmp);
			this.x = _x;
			this.y = _y;	
		}
		
		public function remove() {
			MovieClip(this.parent).removeChild(this);
		}

    }
	
}
