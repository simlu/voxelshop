package 
{

	import flash.events.Event;

	import flash.events.EventDispatcher;

	import flash.utils.getQualifiedClassName;
	
	// a general static event manager

	public class EventCenter extends EventDispatcher
	{

		// this holds the instance that we use
		protected static var _instance:EventCenter;

		// constructor
		public function EventCenter()
		{
			// this is a singleton!
			if ((getQualifiedClassName(super) == "::EventCenter"))
			{

				throw new ArgumentError("Use getInstance please.");

			}

		}

		// get the instance
		public static function getInstance():EventCenter
		{

			if ((_instance != null))
			{
				return _instance;
			}

			_instance = new EventCenter  ;

			return _instance;

		}

	}

}