package com.pixelatedgames.fos.protobufs {
	import com.netease.protobuf.*;
	use namespace com.netease.protobuf.used_by_generated_code;
	import com.netease.protobuf.fieldDescriptors.*;
	import flash.utils.Endian;
	import flash.utils.IDataInput;
	import flash.utils.IDataOutput;
	import flash.utils.IExternalizable;
	import flash.errors.IOError;
	import com.pixelatedgames.fos.protobufs.FantasyMessageType;
	// @@protoc_insertion_point(imports)

	// @@protoc_insertion_point(class_metadata)
	public dynamic final class Position extends com.netease.protobuf.Message {
		/**
		 *  @private
		 */
		public static const TYPE:FieldDescriptor$TYPE_ENUM = new FieldDescriptor$TYPE_ENUM("com.pixelatedgames.fos.protobufs.Position.type", "type", (1 << 3) | com.netease.protobuf.WireType.VARINT, com.pixelatedgames.fos.protobufs.FantasyMessageType);

		private var type$field:int;

		private var hasField$0:uint = 0;

		public function clearType():void {
			hasField$0 &= 0xfffffffe;
			type$field = new int();
		}

		public function get hasType():Boolean {
			return (hasField$0 & 0x1) != 0;
		}

		public function set type(value:int):void {
			hasField$0 |= 0x1;
			type$field = value;
		}

		public function get type():int {
			if(!hasType) {
				return com.pixelatedgames.fos.protobufs.FantasyMessageType.POSITION;
			}
			return type$field;
		}

		/**
		 *  @private
		 */
		public static const X:FieldDescriptor$TYPE_INT64 = new FieldDescriptor$TYPE_INT64("com.pixelatedgames.fos.protobufs.Position.x", "x", (2 << 3) | com.netease.protobuf.WireType.VARINT);

		public var x:Int64;

		/**
		 *  @private
		 */
		public static const Y:FieldDescriptor$TYPE_INT64 = new FieldDescriptor$TYPE_INT64("com.pixelatedgames.fos.protobufs.Position.y", "y", (3 << 3) | com.netease.protobuf.WireType.VARINT);

		public var y:Int64;

		/**
		 *  @private
		 */
		override used_by_generated_code final function writeToBuffer(output:com.netease.protobuf.WritingBuffer):void {
			if (hasType) {
				com.netease.protobuf.WriteUtils.writeTag(output, com.netease.protobuf.WireType.VARINT, 1);
				com.netease.protobuf.WriteUtils.write$TYPE_ENUM(output, type$field);
			}
			com.netease.protobuf.WriteUtils.writeTag(output, com.netease.protobuf.WireType.VARINT, 2);
			com.netease.protobuf.WriteUtils.write$TYPE_INT64(output, this.x);
			com.netease.protobuf.WriteUtils.writeTag(output, com.netease.protobuf.WireType.VARINT, 3);
			com.netease.protobuf.WriteUtils.write$TYPE_INT64(output, this.y);
			for (var fieldKey:* in this) {
				super.writeUnknown(output, fieldKey);
			}
		}

		/**
		 *  @private
		 */
		override used_by_generated_code final function readFromSlice(input:flash.utils.IDataInput, bytesAfterSlice:uint):void {
			var type$count:uint = 0;
			var x$count:uint = 0;
			var y$count:uint = 0;
			while (input.bytesAvailable > bytesAfterSlice) {
				var tag:uint = com.netease.protobuf.ReadUtils.read$TYPE_UINT32(input);
				switch (tag >> 3) {
				case 1:
					if (type$count != 0) {
						throw new flash.errors.IOError('Bad data format: Position.type cannot be set twice.');
					}
					++type$count;
					this.type = com.netease.protobuf.ReadUtils.read$TYPE_ENUM(input);
					break;
				case 2:
					if (x$count != 0) {
						throw new flash.errors.IOError('Bad data format: Position.x cannot be set twice.');
					}
					++x$count;
					this.x = com.netease.protobuf.ReadUtils.read$TYPE_INT64(input);
					break;
				case 3:
					if (y$count != 0) {
						throw new flash.errors.IOError('Bad data format: Position.y cannot be set twice.');
					}
					++y$count;
					this.y = com.netease.protobuf.ReadUtils.read$TYPE_INT64(input);
					break;
				default:
					super.readUnknown(input, tag);
					break;
				}
			}
		}

	}
}
