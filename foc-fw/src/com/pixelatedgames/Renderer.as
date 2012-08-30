package com.pixelatedgames {
	import flash.display.Stage;
	import flash.display.Stage3D;
	import flash.display3D.Context3D;
	import flash.display3D.Context3DRenderMode;
	import flash.system.Capabilities;
	import flash.events.*;
	import flash.system.ApplicationDomain;
	
	public final class Renderer {
		private var _stage:Stage;
		private var _stage3D:Stage3D;
		private var _context3D:Context3D;
		
		public function Renderer(stage:Stage) {
			_stage = stage;
			init();	
		}
		
		private function init():void {
			var stage3DAvailable:Boolean = ApplicationDomain.currentDomain.hasDefinition("flash.display.Stage3D");
			if (stage3DAvailable) {
				_stage.stage3Ds[0].addEventListener(Event.CONTEXT3D_CREATE, onContext3DCreate);
				_stage.stage3Ds[0].addEventListener(ErrorEvent.ERROR, onStage3DError);
				_stage.stage3Ds[0].requestContext3D();
			} else {
				trace("stage3DAvailable is false!");
			}			
		}
		
		private function onContext3DCreate(event:Event):void {
			// Remove existing frame handler. Note that a context
			// loss can occur at any time which will force you
			// to recreate all objects we create here.
			// A context loss occurs for instance if you hit
			// CTRL-ALT-DELETE on Windows.
			// It takes a while before a new context is available
			// hence removing the enterFrame handler is important!
			//if (hasEventListener(Event.ENTER_FRAME))
			//	removeEventListener(Event.ENTER_FRAME,enterFrame);
			
			// Obtain the current context
			_stage3D = event.target as Stage3D;
			_context3D =_stage3D.context3D;
			
			if (_context3D == null) {
				trace('ERROR: no context3D - video driver problem?');
				return;				
			}
			
			// detect software mode (html might not have wmode=direct)
			if ((_context3D.driverInfo == Context3DRenderMode.SOFTWARE)
				|| (_context3D.driverInfo.indexOf('oftware')>-1))
			{
				//Context3DRenderMode.AUTO
				trace("Software mode detected!");
			}
			
			trace('Flash 11 Stage3D '
				+'(Molehill) is working perfectly!'
				+'\nFlash Version: '
				+ Capabilities.version
				+ '\n3D mode: ' + _context3D.driverInfo);
			
			// Disabling error checking will drastically improve performance.
			// If set to true, Flash sends helpful error messages regarding
			// AGAL compilation errors, uninitialized program constants, etc.
			_context3D.enableErrorChecking = false;
			/*
			CONFIG::debug
			{
				_context3D.enableErrorChecking = true; // v2
			}	
			*/
			
			// The 3d back buffer size is in pixels
			_context3D.configureBackBuffer(_stage.stageWidth, _stage.stageHeight, 0, true);
		}
		
		private function onStage3DError ( e:ErrorEvent ):void {
			trace("onStage3DError!" + e);
		}		
	}
}