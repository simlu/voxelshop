package com.pixelatedgames.es.systems {
	import flash.utils.Dictionary;
	import com.pixelatedgames.es.*;	

	public class EntitySystem {
		protected var _em:EntityManager;
		
		private var _componentTypesUsed:Array	= new Array();		
		private var _activeEntities:Dictionary	= new Dictionary();
		
		public function EntitySystem(em:EntityManager, ... types) {
			_em = em;
			for (var i:int = 0; i < types.length; i++) {
				_componentTypesUsed.push(types[i]);
			}
		}
		
		public function updateEntityMembership(e:Entity, activeComponentTypes:Dictionary) {
			
		}
	}
}