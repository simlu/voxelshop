    package {
       
       import flash.events.Event;
	   import flash.display.MovieClip;
	   
	   // handles a movie clip event
     
       public class MovieClipEvent extends Event {
         
		  // add movie clip to timeline request
          public static const SHOW_REQUEST:String = "showMovieClipRequest";
         
		  // the movie clip that we dealing with
          public var movieClip:MovieClip;
         
          public function MovieClipEvent(type:String, clip:MovieClip,
                                      bubbles:Boolean=false,
                                      cancelable:Boolean=false) {
             
             super(type, bubbles, cancelable);
             // store
             this.movieClip = clip;
             
          }
               
		  // clone
          public override function clone():Event {
             return new MovieClipEvent(type, movieClip, bubbles, cancelable);
          }
         
		  // toString
          public override function toString():String {
             return formatToString("CustomEvent", "type", "movieClip",
                                   "bubbles", "cancelable", "eventPhase");
          }
       
       }
     
    }

