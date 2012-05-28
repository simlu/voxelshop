package com.pixelatedgames.es.components {
	public class Velocity implements IComponent{
		public static var Type:int 	= 2;
		
		private var _x:int;
		private var _y:int;		
		
		public function Velocity(x:int, y:int) {
			_x = x;
			_y = y;			
		}
		
		public function getType():* {
			return Type;
		}		
	}
}