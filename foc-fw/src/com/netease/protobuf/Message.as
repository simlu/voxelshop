// vim: tabstop=4 shiftwidth=4

// Copyright (c) 2010 , NetEase.com,Inc. All rights reserved.
// Copyright (c) 2012 , Yang Bo. All rights reserved.
//
// Author: Yang Bo (pop.atry@gmail.com)
//
// Use, modification and distribution are subject to the New BSD License
// as listed at <url: http://www.opensource.org/licenses/bsd-license.php >.

package com.netease.protobuf {
	use namespace com.netease.protobuf.used_by_generated_code;
	import flash.errors.IllegalOperationError
	import flash.errors.IOError
	import flash.utils.Dictionary;
	import flash.utils.getDefinitionByName;
	import flash.utils.IDataInput
	import flash.utils.IDataOutput

	public class Message {
		/**
		 * Parse data as a message of this type and merge it with this.
		 *
		 * @param input The source where data are reading from. <p>After calling
		 * this method, <code>input.endian</code> will be changed to <code>
		 * flash.utils.Endian.LITTLE_ENDIAN</code>. If <code>input</code> is a
		 * <code>flash.utils.ByteArray</code>, input.position will increase by
		 * number of bytes being read.</p>
		 */
		public final function mergeFrom(input:IDataInput):void {
			input.endian = flash.utils.Endian.LITTLE_ENDIAN
			used_by_generated_code::readFromSlice(input, 0)
		}
		/**
		 * Like <code>mergeFrom()</code>, but does not read until EOF. Instead,
		 * the size of the message (encoded as a varint) is read first, then
		 * the message data. Use <code>writeDelimitedTo()</code> to write
		 * messages in this format.
		 *
		 * @param input The source where data are reading from. <p>After calling
		 * this method, <code>input.endian</code> will be changed to <code>
		 * flash.utils.Endian.LITTLE_ENDIAN</code>. If <code>input</code> is a
		 * <code>flash.utils.ByteArray</code>, input.position will increase by
		 * number of bytes being read.</p>
		 *
		 * @see #mergeFrom()
		 * @see #writeDelimitedTo()
		 */
		public final function mergeDelimitedFrom(input:IDataInput):void {
			input.endian = flash.utils.Endian.LITTLE_ENDIAN
			ReadUtils.read$TYPE_MESSAGE(input, this)
		}
		/**
		 * Serializes the message and writes it to <code>output</code>.
		 * 
		 * <p>
		 * NOTE: Protocol Buffers are not self-delimiting. Therefore, if you
		 * write any more data to the stream after the message, you must 
		 * somehow ensure that the parser on the receiving end does not
		 * interpret this as being * part of the protocol message. This can be
		 * done e.g. by writing the size of the message before the data, then
		 * making sure to limit the input to that size on the receiving end
		 * (e.g. by wrapping the InputStream in one which limits the input).
		 * Alternatively, just use <code>writeDelimitedTo()</code>.
		 * </p>
		 *
		 * @param output The destination where data are writing to. <p>If <code>
		 * output</code> is a <code>flash.utils.ByteArray</code>, <code>
		 * output.position</code> will increase by number of bytes being
		 * written.</p>
		 * 
		 * @see #writeDelimitedTo()
		 */
		public final function writeTo(output:IDataOutput):void {
			const buffer:com.netease.protobuf.WritingBuffer = new com.netease.protobuf.WritingBuffer()
			used_by_generated_code::writeToBuffer(buffer)
			buffer.toNormal(output)
		}

		/**
		 * Like <code>writeTo()</code>, but writes the size of the message as
		 * a varint before writing the data. This allows more data to be
		 * written to the stream after the message without the need to delimit
		 * the message data yourself. Use <code>mergeDelimitedFrom()</code> to
		 * parse messages written by this method.
		 *
		 * @param output The destination where data are writing to. <p>If <code>
		 * output</code> is a <code>flash.utils.ByteArray</code>, <code>
		 * output.position</code> will increase by number of bytes being
		 * written.</p>
		 * 
		 * @see #writeTo()
		 * @see #mergeDelimitedFrom()
		 */
		public final function writeDelimitedTo(output:IDataOutput):void {
			const buffer:com.netease.protobuf.WritingBuffer = new com.netease.protobuf.WritingBuffer()
			WriteUtils.write$TYPE_MESSAGE(buffer, this)
			buffer.toNormal(output)
		}

		/**
		 * @private
		 */
		used_by_generated_code function readFromSlice(
				input:IDataInput, bytesAfterSlice:uint):void {
			throw new IllegalOperationError("Not implemented!")
		}

		/**
		 * @private
		 */
		used_by_generated_code function writeToBuffer(
				output:WritingBuffer):void {
			throw new IllegalOperationError("Not implemented!")
		}

		private function writeSingleUnknown(output:WritingBuffer, tag:uint,
				value:*):void {
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
		
		/**
		 * @private
		 */
		protected final function writeUnknown(output:WritingBuffer,
				fieldName:String):void {
			const tag:uint = uint(fieldName)
			if (tag == 0) {
				throw new ArgumentError(
						"Attemp to write an undefined string filed: " +
						fieldName)
			}
			WriteUtils.writeUnknownPair(output, tag, this[fieldName])
		}
		/**
		 * @private
		 */
		protected final function writeExtensionOrUnknown(output:WritingBuffer,
				fieldName:String):void {
			var fieldDescriptor:BaseFieldDescriptor
			try {
				fieldDescriptor =
						BaseFieldDescriptor.getExtensionByName(fieldName)
			} catch (e:ReferenceError) {
				writeUnknown(output, fieldName)
				return
			}
			fieldDescriptor.write(output, this)
		}
		/**
		 * @private
		 */
		protected final function readUnknown(input:IDataInput, tag:uint):void {
			var value:*
			switch (tag & 7) {
			case WireType.VARINT:
				value = ReadUtils.read$TYPE_UINT64(input)
				break
			case WireType.FIXED_64_BIT:
				value = ReadUtils.read$TYPE_FIXED64(input)
				break
			case WireType.LENGTH_DELIMITED:
				value = ReadUtils.read$TYPE_BYTES(input)
				break
			case WireType.FIXED_32_BIT:
				value = ReadUtils.read$TYPE_FIXED32(input)
				break
			default:
				throw new IOError("Invalid wire type: " + (tag & 7))
			}
			const currentValue:* = this[tag]
			if (!currentValue) {
				this[tag] = value
			} else if (currentValue is Array) {
				currentValue.push(value)
			} else {
				this[tag] = [currentValue, value]
			}
		}
		/**
		 * @private
		 */
		protected final function readExtensionOrUnknown(extensions:Array,
				input:IDataInput, tag:uint):void {
			var readFunction:Function = extensions[tag];
			if (readFunction != null) {
				readFunction(input, this);
			} else {
				readUnknown(input, tag)
			}
		}
		
		public function toString():String {
			return TextFormat.printToString(this)
		}
		/**
		 * Get information of a field. 
		 */
		public static function getExtensionByName(
				name:String):IFieldDescriptor {
			return BaseFieldDescriptor.getExtensionByName(name)
		}

	}
}
