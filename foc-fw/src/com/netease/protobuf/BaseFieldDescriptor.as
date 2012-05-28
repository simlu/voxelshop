// vim: tabstop=4 shiftwidth=4

// Copyright (c) 2011 , Yang Bo All rights reserved.
//
// Author: Yang Bo (pop.atry@gmail.com)
//
// Use, modification and distribution are subject to the "New BSD License"
// as listed at <url: http://www.opensource.org/licenses/bsd-license.php >.
package com.netease.protobuf {
	import flash.errors.IllegalOperationError
	import flash.utils.getDefinitionByName;
	import flash.utils.IDataInput
	/**
	 * @private
	 */
	public class BaseFieldDescriptor implements IFieldDescriptor {
		public var fullName:String
		protected var _name:String
		public final function get name():String {
			return _name
		}
		protected var tag:uint
		public final function get tagNumber():uint {
			return tag >>> 3
		}
		public function get type():Class {
			throw new IllegalOperationError("Not Implemented!")
		}
		public function readSingleField(input:IDataInput):* {
			throw new IllegalOperationError("Not Implemented!")
		}
		public function writeSingleField(output:WritingBuffer, value:*):void {
			throw new IllegalOperationError("Not Implemented!")
		}
		public function write(destination:WritingBuffer, source:Message):void {
			throw new IllegalOperationError("Not Implemented!")
		}
		private static const ACTIONSCRIPT_KEYWORDS:Object = {
			"as" : true,		"break" : true,		"case" : true,
			"catch" : true,		"class" : true,		"const" : true,
			"continue" : true,	"default" : true,	"delete" : true,
			"do" : true,		"else" : true,		"extends" : true,
			"false" : true,		"finally" : true,	"for" : true,
			"function" : true,	"if" : true,		"implements" : true,
			"import" : true,	"in" : true,		"instanceof" : true,
			"interface" : true,	"internal" : true,	"is" : true,
			"native" : true,	"new" : true,		"null" : true,
			"package" : true,	"private" : true,	"protected" : true,
			"public" : true,	"return" : true,	"super" : true,
			"switch" : true,	"this" : true,		"throw" : true,
			"to" : true,		"true" : true,		"try" : true,
			"typeof" : true,	"use" : true,		"var" : true,
			"void" : true,		"while" : true,		"with" : true
		}
		
		public function toString():String {
			return name
		}

		internal static function getExtensionByName(
				name:String):BaseFieldDescriptor {
			const fieldPosition:int = name.lastIndexOf('/')
			if (fieldPosition == -1) {
				return BaseFieldDescriptor(getDefinitionByName(name))
			} else {
				return getDefinitionByName(name.substring(0, fieldPosition))[
						name.substring(fieldPosition + 1)]
			}
		}
	}

}
function regexToUpperCase(matched:String, index:int, whole:String):String {
	return matched.charAt(1).toUpperCase()
}
