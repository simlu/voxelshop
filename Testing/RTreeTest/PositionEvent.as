package 
{

	import flash.events.Event;

	// a general position event

	public class PositionEvent extends Event
	{

		// if the view position has been updated
		public static const VIEW_POSITION_CHANGE:String = "viewPosChanged";

		// the location of this position event
		public var x:int;
		public var y:int;

		// constructor
		public function PositionEvent(type:String, ex:int, ey:int,
		                                      bubbles:Boolean=false,
		                                      cancelable:Boolean=false)
		{

			super(type, bubbles, cancelable);

			// assign to variables
			this.x = ex;
			this.y = ey;

		}


		// clone   
		public override function clone():Event
		{
			return new PositionEvent(type, x, y, bubbles, cancelable);
		}

		// toString
		public override function toString():String
		{
			return formatToString("CustomEvent", "type", "x", "y",
			                                   "bubbles", "cancelable", "eventPhase");
		}

	}

}