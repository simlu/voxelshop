/*
import flash.display.Sprite;
import com.pixelatedgames.es.*;
import com.pixelatedgames.es.components.*;
import com.pixelatedgames.fos.*;
//public var em:EntityManager = new EntityManager();
//public var fc:FantasyClient;	
//fc = new FantasyClient();
//fc.connect("127.0.0.1", 443);
//fc.addEventListener(FCEvent.CONNECTED, handleConnected, false, 0, true);

var e:Entity = em.createEntity();
em.addComponent(e, new Position(10,10));
em.addComponent(e, new Velocity(-3,7));

em.updateSystems(e);
private function handleConnected(e:Event):void {
fc.login("Gamer");			
}
*/

package com.pixelatedgames.fos {
	import com.netease.protobuf.*;
	import com.pixelatedgames.fos.protobufs.FantasyMessage;
	import com.pixelatedgames.fos.protobufs.FantasyMessageType;
	import com.pixelatedgames.fos.protobufs.Login;
	
	import flash.events.*;
	import flash.net.Socket;
	import flash.utils.ByteArray;

	public class FantasyClient extends EventDispatcher {
		// base connection
		public var ip:String;		
		public var port:int = 443;
		private var _socket:Socket;
		private var isConnected:Boolean;
		
		// FantasyMessage reading
		private var lengthBuffer:WritingBuffer = new WritingBuffer();
		private var currentPacketLength:int = -1;
		private var byteBuffer:ByteArray = new ByteArray();
		
		// FantasyMessage handling
		private var fantasyMessageHandler:FantasyMessageHandler = new FantasyMessageHandler();
		
		public function FantasyClient() {
			isConnected = false;
			
			_socket = new Socket();			
			_socket.addEventListener(Event.CONNECT, handleSocketConnection, false, 0, true);
			_socket.addEventListener(Event.CLOSE, handleSocketDisconnection, false, 0, true);
			_socket.addEventListener(ProgressEvent.SOCKET_DATA, handleSocketData, false, 0, true);
			_socket.addEventListener(IOErrorEvent.IO_ERROR, handleIOError, false, 0, true);
			_socket.addEventListener(IOErrorEvent.NETWORK_ERROR, handleIOError, false, 0, true);
			_socket.addEventListener(SecurityErrorEvent.SECURITY_ERROR, handleSecurityError, false, 0, true);			
		}
		
		public function connect(ip:String, port:int):void {
			if(!isConnected) {				
				this.ip = ip;
				this.port = port;
				
				_socket.connect(ip, port);							
			} else {
				// debug
			}
		}
		
		public function login(username:String):void {			
			var login:Login = new Login();
			login.username = username;			
									
			var fm:FantasyMessage = new FantasyMessage();
			fm.type = login.type;
			fm.login = login; // this is the problem for abstracting			
								
			// send it
			sendFantasyMessage(fm);
		}
		
		public function sendFantasyMessage(fm:FantasyMessage):void {
			var byteBuff:ByteArray = new ByteArray();
			fm.writeTo(byteBuff);	// write it first to get length (workaround?)
			
			// this is annoying
			var finalByteBuff:WritingBuffer = new WritingBuffer();
			WriteUtils.write$TYPE_INT32(finalByteBuff, byteBuff.length);
			fm.writeTo(finalByteBuff);
			
			// write it
			_socket.writeBytes(finalByteBuff);			
			_socket.flush();				
		}
		
		private function handleSocketData(e:Event):void {
			var bytes:int = _socket.bytesAvailable;			
			var b:int;
			
			// don't have a packet length yet
			if(currentPacketLength < 0) {
				// read a varint
				for (var i:int = 0; i < 5; i ++) {
					if(bytes == 0)
						return;					
					b = _socket.readByte();
					bytes--;
					byteBuffer.writeByte(b);					
					if(b >= 0) {												
						lengthBuffer.writeBytes(byteBuffer);
						lengthBuffer.position = 0;						
						currentPacketLength = ReadUtils.read$TYPE_INT32(lengthBuffer);
						lengthBuffer.clear();
						byteBuffer.clear();
						break;
					}			
				}												
			}					
			
			// read the packet
			while (--bytes >= 0) {
				b = _socket.readByte();				
				byteBuffer.writeByte(b);
				currentPacketLength--;				
				if(currentPacketLength == 0) {
					// reset reader
					currentPacketLength = -1;					
					// move to zero for reading
					byteBuffer.position = 0;										
					// read the message
					var fm:FantasyMessage = new FantasyMessage();					
					fm.mergeFrom(byteBuffer);
					// handle the message
					fantasyMessageHandler.handleFantasyMessage(fm);					
					// clear or new?
					//byteBuffer = new ByteArray()					
					byteBuffer.clear();						
				}
			}			
		}
		
		private function handleSocketConnection(e:Event):void {			
			dispatchEvent(new FCEvent(FCEvent.CONNECTED, {}));		
		}
		
		private function handleSocketDisconnection(e:Event):void {
			// Clear data
			//initialize()
			
			dispatchEvent(new FCEvent(FCEvent.DISCONNECTED, {}));			
		}
		
		private function handleIOError(e:IOErrorEvent):void {
			trace(e);
			//tryBlueBoxConnection(evt)
		}
		
		private function handleSocketError(e:SecurityErrorEvent):void {
			trace(e);
			//debugMessage("Socket Error: " + evt.text)
		}
		
		private function handleSecurityError(e:SecurityErrorEvent):void {
			trace(e);
			//tryBlueBoxConnection(evt)
		}		
	}
}