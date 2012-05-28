package com.pixelatedgames.es {
	import com.pixelatedgames.es.components.*;
	
	import flash.utils.Dictionary;	
	public final class EntityManager {
		
		private var activeEntities:Dictionary			= new Dictionary();
		private var removedAndAvailable:Dictionary		= new Dictionary();
		
		private var nextAvailableId:uint				= 0;
		private var count:uint							= 0;
		private var totalCreated:uint					= 0;
		private var totalRemoved:uint					= 0;
		
		private var activeComponentTypes:Dictionary		= new Dictionary();
		
		public function EntityManager() {
		}
		
		public function createEntity():Entity {
			var e:Entity =  new Entity(nextAvailableId++);
			activeEntities[e.id] = e;
			
			count++;
			totalCreated++;
			
			return e;
		}
		
		public function addComponent(e:Entity, c:IComponent):void {			
			var activeComponents:Dictionary = activeComponentTypes[c.getType()];
			if(activeComponents == null) {
				activeComponents = new Dictionary();
				activeComponentTypes[c.getType()] = activeComponents;
			}
			
			// only allows for one component of each type per entity (is this ok?)
			activeComponents[e.id] = c;
		}
		
		public function getComponent(e:Entity, type:*):IComponent {
			var activeComponents:Dictionary = activeComponentTypes[type];
			return activeComponents[e.id];
		}
		
		// adds and removes entity from entity systems based on components added
		public function updateSystems(e:Entity):void {
			
		}
	}
}