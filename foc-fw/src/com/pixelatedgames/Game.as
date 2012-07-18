package com.pixelatedgames {
	import flash.display.Stage;
	
	public final class Game {
		private var _renderer:Renderer;
		
		public function Game(stage:Stage) {
			_renderer = new Renderer(stage);
		}
	}
}