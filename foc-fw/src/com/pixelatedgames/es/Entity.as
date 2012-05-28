package com.pixelatedgames.es {
	public final class Entity {
		private var _id:uint;
		
		public function Entity(id:uint) {
			_id = id;
		}
		
		public function get id():uint {
			return _id;
		}
	}
}