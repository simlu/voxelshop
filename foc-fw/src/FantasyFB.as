package {
	import com.pixelatedgames.Game;
	
	import flash.display.Sprite;
	import flash.events.*;
	
	[SWF(width='960',height='544',backgroundColor='#FFFFFF',frameRate='60')]
	public class FantasyFB extends Sprite {
		private var _game:Game;
		
		public function FantasyFB() {
			if (stage != null)
				init();
			else
				addEventListener(Event.ADDED_TO_STAGE, init);
		}
		
		private function init(e:Event = null):void {
			if (hasEventListener(Event.ADDED_TO_STAGE))
				removeEventListener(Event.ADDED_TO_STAGE, init);
			
			_game = new Game(stage);
			
		}
	}
}