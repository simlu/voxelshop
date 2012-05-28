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
	public final class ReadUtils {
		public static function skip(input:IDataInput, wireType:uint):void {
			switch (wireType) {
			case WireType.VARINT:
				while (input.readUnsignedByte() > 0x80) {}
				break
			case WireType.FIXED_64_BIT:
				input.readInt()
				input.readInt()
				break
			case WireType.LENGTH_DELIMITED:
				for (var i:uint = read$TYPE_UINT32(input); i != 0; i--) {
					input.readByte()
				}
				break
			case WireType.FIXED_32_BIT:
				input.readInt()
				break
			default:
				throw new IOError("Invalid wire type: " + wireType)
			}
		}
		public static function read$TYPE_DOUBLE(input:IDataInput):Number {
			return input.readDouble()
		}
		public static function read$TYPE_FLOAT(input:IDataInput):Number {
			return input.readFloat()
		}
		public static function read$TYPE_INT64(input:IDataInput):Int64 {
			const result:Int64 = new Int64
			var b:uint
			var i:uint = 0
			for (;; i += 7) {
				b = input.readUnsignedByte()
				if (i == 28) {
					break
				} else {
					if (b >= 0x80) {
						result.low |= ((b & 0x7f) << i)
					} else {
						result.low |= (b << i)
						return result
					}
				}
			}
			if (b >= 0x80) {
				b &= 0x7f
				result.low |= (b << i)
				result.high = b >>> 4
			} else {
				result.low |= (b << i)
				result.high = b >>> 4
				return result
			}
			for (i = 3;; i += 7) {
				b = input.readUnsignedByte()
				if (i < 32) {
					if (b >= 0x80) {
						result.high |= ((b & 0x7f) << i)
					} else {
						result.high |= (b << i)
						break
					}
				}
			}
			return result
		}
		public static function read$TYPE_UINT64(input:IDataInput):UInt64 {
			const result:UInt64 = new UInt64
			var b:uint
			var i:uint = 0
			for (;; i += 7) {
				b = input.readUnsignedByte()
				if (i == 28) {
					break
				} else {
					if (b >= 0x80) {
						result.low |= ((b & 0x7f) << i)
					} else {
						result.low |= (b << i)
						return result
					}
				}
			}
			if (b >= 0x80) {
				b &= 0x7f
				result.low |= (b << i)
				result.high = b >>> 4
			} else {
				result.low |= (b << i)
				result.high = b >>> 4
				return result
			}
			for (i = 3;; i += 7) {
				b = input.readUnsignedByte()
				if (i < 32) {
					if (b >= 0x80) {
						result.high |= ((b & 0x7f) << i)
					} else {
						result.high |= (b << i)
						break
					}
				}
			}
			return result
		}
		public static function read$TYPE_INT32(input:IDataInput):int {
			return int(read$TYPE_UINT32(input))
		}
		public static function read$TYPE_FIXED64(input:IDataInput):UInt64 {
			const result:UInt64 = new UInt64
			result.low = input.readUnsignedInt()
			result.high = input.readUnsignedInt()
			return result
		}
		public static function read$TYPE_FIXED32(input:IDataInput):uint {
			return input.readUnsignedInt()
		}
		public static function read$TYPE_BOOL(input:IDataInput):Boolean {
			return read$TYPE_UINT32(input) != 0
		}
		public static function read$TYPE_STRING(input:IDataInput):String {
			const length:uint = read$TYPE_UINT32(input)
			return input.readUTFBytes(length)
		}
		public static function read$TYPE_BYTES(input:IDataInput):ByteArray {
			const result:ByteArray = new ByteArray
			const length:uint = read$TYPE_UINT32(input)
			if (length > 0) {
				input.readBytes(result, 0, length)
			}
			return result
		}
		public static function read$TYPE_UINT32(input:IDataInput):uint {
			var result:uint = 0
			for (var i:uint = 0;; i += 7) {
				const b:uint = input.readUnsignedByte()
				if (i < 32) {
					if (b >= 0x80) {
						result |= ((b & 0x7f) << i)
					} else {
						result |= (b << i)
						break
					}
				} else {
					while (input.readUnsignedByte() >= 0x80) {}
					break
				}
			}
			return result
		}
		public static function read$TYPE_ENUM(input:IDataInput):int {
			return read$TYPE_INT32(input)
		}
		public static function read$TYPE_SFIXED32(input:IDataInput):int {
			return input.readInt()
		}
		public static function read$TYPE_SFIXED64(input:IDataInput):Int64 {
			const result:Int64 = new Int64
			result.low = input.readUnsignedInt()
			result.high = input.readInt()
			return result
		}
		public static function read$TYPE_SINT32(input:IDataInput):int {
			return ZigZag.decode32(read$TYPE_UINT32(input))
		}
		public static function read$TYPE_SINT64(input:IDataInput):Int64 {
			const result:Int64 = read$TYPE_INT64(input)
			const low:uint = result.low
			const high:uint = result.high
			result.low = ZigZag.decode64low(low, high)
			result.high = ZigZag.decode64high(low, high)
			return result
		}
		public static function read$TYPE_MESSAGE(input:IDataInput,
				message:Message):Message {
			const length:uint = read$TYPE_UINT32(input)
			if (input.bytesAvailable < length) {
				throw new IOError("Invalid message length: " + length)
			}
			const bytesAfterSlice:uint = input.bytesAvailable - length
			message.used_by_generated_code::readFromSlice(input, bytesAfterSlice)
			if (input.bytesAvailable != bytesAfterSlice) {
				throw new IOError("Invalid nested message")
			}
			return message
		}
		public static function readPackedRepeated(input:IDataInput,
				readFuntion:Function, value:Array):void {
			const length:uint = read$TYPE_UINT32(input)
			if (input.bytesAvailable < length) {
				throw new IOError("Invalid message length: " + length)
			}
			const bytesAfterSlice:uint = input.bytesAvailable - length
			while (input.bytesAvailable > bytesAfterSlice) {
				value.push(readFuntion(input))
			}
			if (input.bytesAvailable != bytesAfterSlice) {
				throw new IOError("Invalid packed repeated data")
			}
		}
	}
}
