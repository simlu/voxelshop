package com.pixelatedgames.fos {
	import flash.events.Event;

	public final class FCEvent extends Event {		
		public static const CONNECTED:String 	= "FCConnected";
		public static const DISCONNECTED:String = "FCDisconnected";
		
		public var params:Object;
		
		public function FCEvent(type:String, params:Object) {
			super(type)
			this.params = params
		}
		
		public override function clone():Event {
			return new FCEvent(this.type, this.params)
		}
		
		public override function toString():String {
			return formatToString("FCEvent", "type", "bubbles", "cancelable", "eventPhase", "params")
		}		
	}
}