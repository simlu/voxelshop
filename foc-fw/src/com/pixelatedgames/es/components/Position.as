package com.pixelatedgames.es.components {
	public class Position implements IComponent {
		public static var Type:int 	= 1;
		
		private var _x:int;
		private var _y:int;
		
		public function Position(x:int, y:int) {
			_x = x;
			_y = y;
		}
		
		public function getType():* {
			return Type;
		}
	}
}