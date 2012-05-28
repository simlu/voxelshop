// vim: tabstop=4 shiftwidth=4

// Copyright (c) 2010 , NetEase.com,Inc. All rights reserved.
//
// Author: Yang Bo (pop.atry@gmail.com)
//
// Use, modification and distribution are subject to the "New BSD License"
// as listed at <url: http://www.opensource.org/licenses/bsd-license.php >.

package com.netease.protobuf {
	import flash.errors.*;
	import flash.utils.*;
	/**
	 * @private
	 */
	public final class WritingBuffer extends ByteArray {
		public function WritingBuffer() {
			endian = Endian.LITTLE_ENDIAN
		}
		/*
			// for Flash Player 9
			[ArrayElementType("uint")]
			private const slices:Array = []
		/*/
			// for Flash Player 10
			private const slices:Vector.<uint> = new Vector.<uint>
		//*/
		public function beginBlock():uint {
			slices.push(position)
			const beginSliceIndex:uint = slices.length
			slices.length += 2
			slices.push(position)
			return beginSliceIndex
		}
		public function endBlock(beginSliceIndex:uint):void {
			slices.push(position)
			const beginPosition:uint = slices[beginSliceIndex + 2]
			slices[beginSliceIndex] = position
			WriteUtils.write$TYPE_UINT32(this, position - beginPosition)
			slices[beginSliceIndex + 1] = position
			slices.push(position)
		}
		public function toNormal(output:IDataOutput):void {
			var i:uint = 0
			var begin:uint = 0
			while (i < slices.length) {
				var end:uint = slices[i]
				++i
				if (end > begin) {
					output.writeBytes(this, begin, end - begin)
				} else if (end < begin) {
					throw new IllegalOperationError
				}
				begin = slices[i]
				++i
			}
			output.writeBytes(this, begin)
		}
	}
}
