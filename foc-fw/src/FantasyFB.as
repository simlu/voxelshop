package {
	import flash.display.Sprite;
	import com.pixelatedgames.es.*;
	import com.pixelatedgames.es.components.*;
	import com.pixelatedgames.fos.*;
	
	import flash.display.Sprite;
	import flash.events.Event;	
	
	public class FantasyFB extends Sprite {
		
		public var em:EntityManager = new EntityManager();
		
		public var fc:FantasyClient;		
		
		public function FantasyFB() {
			fc = new FantasyClient();
			fc.connect("127.0.0.1", 443);
			fc.addEventListener(FCEvent.CONNECTED, handleConnected, false, 0, true);
			
			/*var e:Entity = em.createEntity();
			em.addComponent(e, new Position(10,10));
			em.addComponent(e, new Velocity(-3,7));
			
			em.updateSystems(e);*/
		}
		
		private function handleConnected(e:Event):void {
			fc.login("Gamer");			
		}
	}
}