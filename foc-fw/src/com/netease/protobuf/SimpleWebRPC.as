// vim: tabstop=4 shiftwidth=4

// Copyright (c) 2010 , NetEase.com,Inc. All rights reserved.
//
// Author: Yang Bo (pop.atry@gmail.com)
//
// Use, modification and distribution are subject to the "New BSD License"
// as listed at <url: http://www.opensource.org/licenses/bsd-license.php >.

package com.netease.protobuf {
	import flash.net.*;
	import flash.utils.*;
	import flash.events.*;
	/**
	 * A simple sample of RPC implementation.
	 */
	public final class SimpleWebRPC {
		private var urlPrefix:String
		public function SimpleWebRPC(urlPrefix:String) {
			this.urlPrefix = urlPrefix
		}

		private static const REF:Dictionary = new Dictionary

		public function send(qualifiedMethodName:String,
							 requestMessage:Message,
							 rpcResult:Function,
							 responseClass:Class):void {
			const loader:URLLoader = new URLLoader
			REF[loader] = true;
			loader.dataFormat = URLLoaderDataFormat.BINARY
			loader.addEventListener(Event.COMPLETE, function(event:Event):void {
				delete REF[loader]
				const responseMessage:Message = new responseClass
				responseMessage.mergeFrom(loader.data)
				rpcResult(responseMessage)
			})
			function errorEventHandler(event:Event):void {
				delete REF[loader]
				rpcResult(event)
			}
			loader.addEventListener(IOErrorEvent.IO_ERROR, errorEventHandler)
			loader.addEventListener(
					SecurityErrorEvent.SECURITY_ERROR, errorEventHandler)
			const request:URLRequest = new URLRequest(
				urlPrefix + qualifiedMethodName.replace(/\./g, "/").
					replace(/^((com|org|net)\/\w+\/\w+\/)?(.*)$/, "$3"))
			const requestContent:ByteArray = new ByteArray
			requestMessage.writeTo(requestContent)
			if (requestContent.length != 0)
			{
				request.data = requestContent
			}
			request.contentType = "application/x-protobuf"
			request.method = URLRequestMethod.POST
			loader.load(request)
		}
	}
}
