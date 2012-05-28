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
	import com.pixelatedgames.fos.protobufs.Position;
	import com.pixelatedgames.fos.protobufs.Login;
	// @@protoc_insertion_point(imports)

	// @@protoc_insertion_point(class_metadata)
	public dynamic final class FantasyMessage extends com.netease.protobuf.Message {
		/**
		 *  @private
		 */
		public static const TYPE:FieldDescriptor$TYPE_ENUM = new FieldDescriptor$TYPE_ENUM("com.pixelatedgames.fos.protobufs.FantasyMessage.type", "type", (1 << 3) | com.netease.protobuf.WireType.VARINT, com.pixelatedgames.fos.protobufs.FantasyMessageType);

		public var type:int;

		/**
		 *  @private
		 */
		public static const LOGIN:FieldDescriptor$TYPE_MESSAGE = new FieldDescriptor$TYPE_MESSAGE("com.pixelatedgames.fos.protobufs.FantasyMessage.login", "login", (2 << 3) | com.netease.protobuf.WireType.LENGTH_DELIMITED, com.pixelatedgames.fos.protobufs.Login);

		private var login$field:com.pixelatedgames.fos.protobufs.Login;

		public function clearLogin():void {
			login$field = null;
		}

		public function get hasLogin():Boolean {
			return login$field != null;
		}

		public function set login(value:com.pixelatedgames.fos.protobufs.Login):void {
			login$field = value;
		}

		public function get login():com.pixelatedgames.fos.protobufs.Login {
			return login$field;
		}

		/**
		 *  @private
		 */
		public static const POSITION:FieldDescriptor$TYPE_MESSAGE = new FieldDescriptor$TYPE_MESSAGE("com.pixelatedgames.fos.protobufs.FantasyMessage.position", "position", (3 << 3) | com.netease.protobuf.WireType.LENGTH_DELIMITED, com.pixelatedgames.fos.protobufs.Position);

		private var position$field:com.pixelatedgames.fos.protobufs.Position;

		public function clearPosition():void {
			position$field = null;
		}

		public function get hasPosition():Boolean {
			return position$field != null;
		}

		public function set position(value:com.pixelatedgames.fos.protobufs.Position):void {
			position$field = value;
		}

		public function get position():com.pixelatedgames.fos.protobufs.Position {
			return position$field;
		}

		/**
		 *  @private
		 */
		override used_by_generated_code final function writeToBuffer(output:com.netease.protobuf.WritingBuffer):void {
			com.netease.protobuf.WriteUtils.writeTag(output, com.netease.protobuf.WireType.VARINT, 1);
			com.netease.protobuf.WriteUtils.write$TYPE_ENUM(output, this.type);
			if (hasLogin) {
				com.netease.protobuf.WriteUtils.writeTag(output, com.netease.protobuf.WireType.LENGTH_DELIMITED, 2);
				com.netease.protobuf.WriteUtils.write$TYPE_MESSAGE(output, login$field);
			}
			if (hasPosition) {
				com.netease.protobuf.WriteUtils.writeTag(output, com.netease.protobuf.WireType.LENGTH_DELIMITED, 3);
				com.netease.protobuf.WriteUtils.write$TYPE_MESSAGE(output, position$field);
			}
			for (var fieldKey:* in this) {
				super.writeUnknown(output, fieldKey);
			}
		}

		/**
		 *  @private
		 */
		override used_by_generated_code final function readFromSlice(input:flash.utils.IDataInput, bytesAfterSlice:uint):void {
			var type$count:uint = 0;
			var login$count:uint = 0;
			var position$count:uint = 0;
			while (input.bytesAvailable > bytesAfterSlice) {
				var tag:uint = com.netease.protobuf.ReadUtils.read$TYPE_UINT32(input);
				switch (tag >> 3) {
				case 1:
					if (type$count != 0) {
						throw new flash.errors.IOError('Bad data format: FantasyMessage.type cannot be set twice.');
					}
					++type$count;
					this.type = com.netease.protobuf.ReadUtils.read$TYPE_ENUM(input);
					break;
				case 2:
					if (login$count != 0) {
						throw new flash.errors.IOError('Bad data format: FantasyMessage.login cannot be set twice.');
					}
					++login$count;
					this.login = new com.pixelatedgames.fos.protobufs.Login();
					com.netease.protobuf.ReadUtils.read$TYPE_MESSAGE(input, this.login);
					break;
				case 3:
					if (position$count != 0) {
						throw new flash.errors.IOError('Bad data format: FantasyMessage.position cannot be set twice.');
					}
					++position$count;
					this.position = new com.pixelatedgames.fos.protobufs.Position();
					com.netease.protobuf.ReadUtils.read$TYPE_MESSAGE(input, this.position);
					break;
				default:
					super.readUnknown(input, tag);
					break;
				}
			}
		}

	}
}
