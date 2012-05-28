package com.pixelatedgames.es.systems {
	import flash.utils.Dictionary;
	import com.pixelatedgames.es.*;
	import com.pixelatedgames.es.components.*;	

	public final class MovementSystem extends EntitySystem {		
		
		public function MovementSystem(em:EntityManager) {
			super(em, Position.Type, Velocity.Type);
		}
		
		protected function process(e:Entity):void {
			var position:Position = _em.getComponent(e, Position.Type);
			var velocity:Velocity = _em.getComponent(e, Velocity.Type);
			
			
		}
	}
}