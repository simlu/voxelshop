// vim: tabstop=4 shiftwidth=4

// Copyright (c) 2011 , Yang Bo All rights reserved.
//
// Author: Yang Bo (pop.atry@gmail.com)
//
// Use, modification and distribution are subject to the "New BSD License"
// as listed at <url: http://www.opensource.org/licenses/bsd-license.php >.

package com.netease.protobuf {
	import flash.errors.IllegalOperationError;
	import flash.utils.IDataInput
	/**
	 * @private
	 */
	public class FieldDescriptor extends BaseFieldDescriptor {
		public final function read(input:IDataInput,
				message:Message):void {
			message[name] = readSingleField(input)
		}
		override public final function write(output:WritingBuffer,
				message:Message):void {
			WriteUtils.write$TYPE_UINT32(output, tag)
			writeSingleField(output, message[name])
		}
	}
}
