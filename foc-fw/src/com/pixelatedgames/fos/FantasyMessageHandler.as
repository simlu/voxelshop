package com.pixelatedgames.fos {
	import com.pixelatedgames.fos.protobufs.FantasyMessage;

	public final class FantasyMessageHandler {
		
		public function FantasyMessageHandler() {
		}
		
		public function handleFantasyMessage(fm:FantasyMessage):void {
			trace(fm.type);
		}
	}
}