// vim: tabstop=4 shiftwidth=4

// Copyright (c) 2010 , NetEase.com,Inc. All rights reserved.
// Copyright (c) 2012 , Yang Bo. All rights reserved.
//
// Author: Yang Bo (pop.atry@gmail.com)
//
// Use, modification and distribution are subject to the "New BSD License"
// as listed at <url: http://www.opensource.org/licenses/bsd-license.php >.

package com.netease.protobuf {
	import flash.errors.*
	import flash.utils.*
	/**
	 * @private
	 */
	public final class WriteUtils {
		private static function writeSingleUnknown(output:WritingBuffer,
				tag:uint, value:*):void {
			WriteUtils.write$TYPE_UINT32(output, tag)
			switch (tag & 7) {
			case WireType.VARINT:
				WriteUtils.write$TYPE_UINT64(output, value)
				break
			case WireType.FIXED_64_BIT:
				WriteUtils.write$TYPE_FIXED64(output, value)
				break
			case WireType.LENGTH_DELIMITED:
				WriteUtils.write$TYPE_BYTES(output, value)
				break
			case WireType.FIXED_32_BIT:
				WriteUtils.write$TYPE_FIXED32(output, value)
				break
			default:
				throw new IOError("Invalid wire type: " + (tag & 7))
			}
		}

		public static function writeUnknownPair(output:WritingBuffer, tag:uint,
				value:*):void {
			const repeated:Array = value as Array
			if (repeated) {
				for each (var element:* in repeated) {
					writeSingleUnknown(output, tag, element)
				}
			} else {
				writeSingleUnknown(output, tag, value)
			}
		}

		private static function writeVarint64(output:WritingBuffer,
			low:uint, high:uint):void {
			if (high == 0) {
				write$TYPE_UINT32(output, low)
			} else {
				for (var i:uint = 0; i < 4; ++i) {
					output.writeByte((low & 0x7F) | 0x80)
					low >>>= 7
				}
				if ((high & (0xFFFFFFF << 3)) == 0) {
					output.writeByte((high << 4) | low)
				} else {
					output.writeByte((((high << 4) | low) & 0x7F) | 0x80)
					write$TYPE_UINT32(output, high >>> 3)
				}
			}
		}
		public static function writeTag(output:WritingBuffer,
				wireType:uint, number:uint):void {
			write$TYPE_UINT32(output, (number << 3) | wireType)
		}
		public static function write$TYPE_DOUBLE(output:WritingBuffer, value:Number):void {
			output.writeDouble(value)
		}
		public static function write$TYPE_FLOAT(output:WritingBuffer, value:Number):void {
			output.writeFloat(value)
		}
		public static function write$TYPE_INT64(output:WritingBuffer, value:Int64):void {
			writeVarint64(output, value.low, value.high)
		}
		public static function write$TYPE_UINT64(output:WritingBuffer, value:UInt64):void {
			writeVarint64(output, value.low, value.high)
		}
		public static function write$TYPE_INT32(output:WritingBuffer, value:int):void {
			if (value < 0) {
				writeVarint64(output, value, 0xFFFFFFFF)
			} else {
				write$TYPE_UINT32(output, value)
			}
		}
		public static function write$TYPE_FIXED64(output:WritingBuffer, value:UInt64):void {
			output.writeUnsignedInt(value.low)
			output.writeUnsignedInt(value.high)
		}
		public static function write$TYPE_FIXED32(output:WritingBuffer, value:uint):void {
			output.writeUnsignedInt(value)
		}
		public static function write$TYPE_BOOL(output:WritingBuffer, value:Boolean):void {
			output.writeByte(value ? 1 : 0)
		}
		public static function write$TYPE_STRING(output:WritingBuffer, value:String):void {
			const i:uint = output.beginBlock()
			output.writeUTFBytes(value)
			output.endBlock(i)
		}
		public static function write$TYPE_BYTES(output:WritingBuffer, value:ByteArray):void {
			write$TYPE_UINT32(output, value.length)
			output.writeBytes(value)
		}
		public static function write$TYPE_UINT32(output:WritingBuffer, value:uint):void {
			for (;;) {
				if (value < 0x80) {
					output.writeByte(value)
					return;
				} else {
					output.writeByte((value & 0x7F) | 0x80)
					value >>>= 7
				}
			}
		}
		public static function write$TYPE_ENUM(output:WritingBuffer, value:int):void {
			write$TYPE_INT32(output, value)
		}
		public static function write$TYPE_SFIXED32(output:WritingBuffer, value:int):void {
			output.writeInt(value)
		}
		public static function write$TYPE_SFIXED64(output:WritingBuffer, value:Int64):void {
			output.writeUnsignedInt(value.low)
			output.writeInt(value.high)
		}
		public static function write$TYPE_SINT32(output:WritingBuffer, value:int):void {
			write$TYPE_UINT32(output, ZigZag.encode32(value))
		}
		public static function write$TYPE_SINT64(output:WritingBuffer, value:Int64):void {
			writeVarint64(output,
					ZigZag.encode64low(value.low, value.high),
					ZigZag.encode64high(value.low, value.high))
		}
		public static function write$TYPE_MESSAGE(output:WritingBuffer, value:Message):void {
			const i:uint = output.beginBlock()
			value.used_by_generated_code::writeToBuffer(output)
			output.endBlock(i)
		}
		public static function writePackedRepeated(output:WritingBuffer,
				writeFunction:Function, value:Array):void {
			const i:uint = output.beginBlock()
			for (var j:uint = 0; j < value.length; j++) {
				writeFunction(output, value[j])
			}
			output.endBlock(i)
		}
	}
}
