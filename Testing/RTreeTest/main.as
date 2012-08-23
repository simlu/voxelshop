package 
{
	import flash.display.MovieClip;
	import org.ffilmation.utils.rtree.fCube;
	import org.ffilmation.utils.rtree.fRTree;
	import flash.events.Event;
	import flash.events.KeyboardEvent;
	//import flash.display.StageAlign;
    import flash.display.StageScaleMode; 
	import flash.display.Bitmap;

	public class main extends MovieClip
	{
		// manages the world objects
		var plankmanager:PlankManager;
		
		// world view position
		var worldx:int = -stage.stageWidth / 2;
		var worldy:int = -stage.stageHeight / 2;
		
		// key stages
		var leftKeyDown:Boolean = false;
		var rightKeyDown:Boolean = false;
		var upKeyDown:Boolean = false;
		var downKeyDown:Boolean = false;
		
		// adds the send movieclip to the main timeline
		public function addMovieClipEvent(ev:MovieClipEvent) {
			this.addChild(ev.movieClip);
		}
		
		// frame event
		public function onFrameEnter(ev:Event) {
			if (leftKeyDown) {
					worldx-=5;
					plankmanager.setViewPos(worldx,worldy);
			}
			if (rightKeyDown) {
					worldx+=5;
					plankmanager.setViewPos(worldx,worldy);
			}
			if (upKeyDown) {
					worldy-=5;
					plankmanager.setViewPos(worldx,worldy);
			}
			if (downKeyDown) {
					worldy+=5;
					plankmanager.setViewPos(worldx,worldy);
			}
		}
		
		// handle key events
		public function onKeyDown(ev:KeyboardEvent) {
			switch (ev.keyCode) {
				// left
				case 37:
					leftKeyDown = true;
				break;
				// right
				case 39:
					rightKeyDown = true;
				break;
				// up
				case 38:
					upKeyDown = true;
				break;
				// down
				case 40:
					downKeyDown = true;
				break;
			}
		}
		
				// handle key events
		public function onKeyUp(ev:KeyboardEvent) {
			switch (ev.keyCode) {
				// left
				case 37:
					leftKeyDown = false;
				break;
				// right
				case 39:
					rightKeyDown = false;
				break;
				// up
				case 38:
					upKeyDown = false;
				break;
				// down
				case 40:
					downKeyDown = false;
				break;
			}
		}
		
		public function main()
		{
			// set the scalemode
			stage.scaleMode = StageScaleMode.EXACT_FIT;
			//stage.scaleMode = StageScaleMode.NO_SCALE;
			//stage.align = StageAlign.TOP;
			// register the listener (to add object to main view)
			EventCenter.getInstance().addEventListener(MovieClipEvent.SHOW_REQUEST, addMovieClipEvent);
			plankmanager = new PlankManager(stage.stageWidth, stage.stageHeight);
			// set the view position
			plankmanager.setViewPos(worldx,worldy);
			// register key events
			addEventListener('keyDown', onKeyDown);
			addEventListener('keyUp', onKeyUp);
			stage.addEventListener(KeyboardEvent.KEY_DOWN, onKeyDown);
			stage.addEventListener(KeyboardEvent.KEY_UP, onKeyUp);
			// the enter frame event
			stage.addEventListener(Event.ENTER_FRAME, onFrameEnter);
			
			var count:int = 20;
			for (var i:int = 0; i < count; i++) {
				for (var j:int = 0; j < count; j++) {
					plankmanager.addTile("z0x0y0.png", i * 32, j * 32, i * count + j);
				}
			}
			
			
		}

	}

}