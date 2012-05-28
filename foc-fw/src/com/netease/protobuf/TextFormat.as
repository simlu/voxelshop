// vim: tabstop=4 shiftwidth=4

// Copyright (c) 2011 , Yang Bo All rights reserved.
//
// Author: Yang Bo (pop.atry@gmail.com)
//
// Use, modification and distribution are subject to the "New BSD License"
// as listed at <url: http://www.opensource.org/licenses/bsd-license.php >.

package com.netease.protobuf {
	import com.netease.protobuf.fieldDescriptors.*;
	import flash.errors.IllegalOperationError;
	import flash.errors.IOError;
	import flash.utils.describeType
	import flash.utils.Dictionary;
	import flash.utils.getDefinitionByName;
	import flash.utils.IDataInput
	import flash.utils.IDataOutput
	import flash.utils.ByteArray
	public final class TextFormat {
		private static function printHex(output:IDataOutput, value:uint):void {
			const hexString:String = value.toString(16)
			output.writeUTFBytes("00000000".substring(0, 8 - hexString.length))
			output.writeUTFBytes(hexString)
		}
		private static const allEnumValues:Dictionary = new Dictionary
		private static function printEnum(output:IDataOutput,
				value:int, enumType:Class):void {
			var enumValues:Array
			if (enumType in allEnumValues) {
				enumValues = allEnumValues[enumType]
			} else {
				const enumTypeDescription:XML = describeType(enumType)
				// Not enumTypeDescription.*.@name,
				// because haXe will replace all constants to variables, WTF!
				const xmlNames:XMLList = enumTypeDescription.*.@name
				enumValues = []
				for each(var name:String in xmlNames) {
					enumValues[enumType[name]] = name
				}
				allEnumValues[enumType] = enumValues
			}
			if (value in enumValues) {
				output.writeUTFBytes(enumValues[value])
			} else {
				throw new IOError(value + " is invalid for " +
						enumTypeDescription.@name)
			}
		}
		private static function printBytes(output:IDataOutput,
				value:ByteArray):void {
			output.writeUTFBytes("\"");
			value.position = 0
			while (value.bytesAvailable > 0) {
				const byte:int = value.readByte()
				switch (byte) {
				case 7: output.writeUTFBytes("\\a" ); break;
				case 8: output.writeUTFBytes("\\b" ); break;
				case 12: output.writeUTFBytes("\\f" ); break;
				case 10: output.writeUTFBytes("\\n" ); break;
				case 13: output.writeUTFBytes("\\r" ); break;
				case 9: output.writeUTFBytes("\\t" ); break;
				case 11: output.writeUTFBytes("\\v" ); break;
				case 92: output.writeUTFBytes("\\\\"); break;
				case 39: output.writeUTFBytes("\\\'"); break;
				case 34 : output.writeUTFBytes("\\\""); break;
				default:
					if (byte >= 0x20) {
						output.writeByte(byte);
					} else {
						output.writeUTFBytes('\\');
						output.writeByte('0'.charCodeAt() + ((byte >>> 6) & 3));
						output.writeByte('0'.charCodeAt() + ((byte >>> 3) & 7));
						output.writeByte('0'.charCodeAt() + (byte & 7));
					}
					break;
				}
			}
			output.writeUTFBytes("\"");
		}
		private static function printString(output:IDataOutput,
				value:String):void {
			const buffer:ByteArray = new ByteArray
			buffer.writeUTFBytes(value)
			printBytes(output, buffer)
		}
		private static function printUnknownField(output:IDataOutput, tag:uint,
				value:Object, printSetting:PrintSetting, currentIndent:String):void {
			const unknownArray:Array = value as Array
			if (unknownArray) {
				if (unknownArray.length > 0) {
					printSingleUnknownField(output, tag, unknownArray[k],
							printSetting, currentIndent)
					for (var k:int = 1; k < unknownArray.length; k++) {
						output.writeByte(printSetting.newLine)
						printSingleUnknownField(output, tag, unknownArray[k],
								printSetting, currentIndent)
					}
				}
			} else {
				printSingleUnknownField(
						output, tag, value, printSetting,
						currentIndent)
			}
		}
					
		private static function printSingleUnknownField(output:IDataOutput,
				tag:uint, value:Object, printSetting:PrintSetting,
				currentIndent:String):void {
			output.writeUTFBytes(currentIndent)
			output.writeUTFBytes(String(tag >>> 3))
			output.writeUTFBytes(printSetting.simpleFieldSeperator)
			switch (tag & 7) {
			case WireType.VARINT:
				output.writeUTFBytes(UInt64(value).toString())
				break
			case WireType.FIXED_32_BIT:
				output.writeUTFBytes("0x")
				printHex(output, uint(value))
				break
			case WireType.FIXED_64_BIT:
				const u64:UInt64 = UInt64(value)
				output.writeUTFBytes("0x")
				printHex(output, u64.high)
				printHex(output, u64.low)
				break
			case WireType.LENGTH_DELIMITED:
				printBytes(output, ByteArray(value))
				break
			}
		}
		private static const allMessageFields:Dictionary = new Dictionary
		private static function printMessageFields(output:IDataOutput,
				message:Message,
				printSetting:PrintSetting,
				currentIndent:String = ""):void {
			var isFirst:Boolean = true
			const type:Class = Object(message).constructor
			var messageFields:XMLList
			if (type in allMessageFields) {
				// Fetch in cache
				messageFields = allMessageFields[type]
			} else {
				const description:XML = describeType(type)
				// Not description.constant,
				// because haXe will replace constant to variable, WTF!
				messageFields = description.*.
					(0 == String(@type).search(
						/^com.netease.protobuf.fieldDescriptors::(Repeated)?FieldDescriptor\$/) &&
						BaseFieldDescriptor(type[@name]).name.
								search(/^\./) == -1// Not extension
					).@name
				allMessageFields[type] = messageFields
			}
			
			for each (var fieldDescriptorName:String in messageFields) {
				const fieldDescriptor:BaseFieldDescriptor =
						type[fieldDescriptorName]
				const shortName:String = fieldDescriptor.fullName.substring(
						fieldDescriptor.fullName.lastIndexOf('.') + 1)
				if (fieldDescriptor.type == Array) {
					const fieldValues:Array = message[fieldDescriptor.name]
					if (fieldValues) {
						for (var i:int = 0; i < fieldValues.length; i++) {
							if (isFirst) {
								isFirst = false
							} else {
								output.writeByte(printSetting.newLine)
							}
							output.writeUTFBytes(currentIndent)
							output.writeUTFBytes(shortName)
							printValue(output, fieldDescriptor, fieldValues[i],
									printSetting, currentIndent)
						}
					}
				} else {
					const m:Array = fieldDescriptor.name.match(/^(__)?(.)(.*)$/)
					m[0] = ""
					m[1] = "has"
					m[2] = m[2].toUpperCase()
					const hasField:String = m.join("")
					try {
						// optional and does not have that field.
						if (false === message[hasField]) {
							continue
						}
					} catch (e:ReferenceError) {
						// required
					}
					if (isFirst) {
						isFirst = false
					} else {
						output.writeByte(printSetting.newLine)
					}
					output.writeUTFBytes(currentIndent)
					output.writeUTFBytes(shortName)
					printValue(output, fieldDescriptor,
							message[fieldDescriptor.name], printSetting,
							currentIndent)
				}
			}
			for (var key:String in message) {
				var extension:BaseFieldDescriptor
				try {
					extension = BaseFieldDescriptor.getExtensionByName(key)
				} catch (e:ReferenceError) {
					if (key.search(/^[0-9]+$/) == 0) {
						// unknown field
						if (isFirst) {
							isFirst = false
						} else {
							output.writeByte(printSetting.newLine)
						}
						printUnknownField(output, uint(key), message[key],
								printSetting, currentIndent)
					} else {
						throw new IOError("Bad unknown field " + key)
					}
					continue
				}
				if (extension.type == Array) {
					const extensionFieldValues:Array = message[key]
					for (var j:int = 0; j < extensionFieldValues.length; j++) {
						if (isFirst) {
							isFirst = false
						} else {
							output.writeByte(printSetting.newLine)
						}
						output.writeUTFBytes(currentIndent)
						output.writeUTFBytes("[")
						output.writeUTFBytes(extension.fullName)
						output.writeUTFBytes("]")
						printValue(output, extension,
								extensionFieldValues[j], printSetting,
								currentIndent)
					}
				} else {
					if (isFirst) {
						isFirst = false
					} else {
						output.writeByte(printSetting.newLine)
					}
					output.writeUTFBytes(currentIndent)
					output.writeUTFBytes("[")
					output.writeUTFBytes(extension.fullName)
					output.writeUTFBytes("]")
					printValue(output, extension, message[key], printSetting,
							currentIndent)
				}
			}
		}
		
		private static function printValue(output:IDataOutput,
				fieldDescriptor:BaseFieldDescriptor,
				value:Object,
				printSetting:PrintSetting,
				currentIndent:String = ""):void {
			const message:Message = value as Message
			if (message) {
				if (printSetting == SINGLELINE_MODE) {
					output.writeUTFBytes("{")
				} else {
					output.writeUTFBytes(" {\n")
				}
				printMessageFields(output, message, printSetting,
						printSetting.indentChars + currentIndent)
				if (printSetting == SINGLELINE_MODE) {
					output.writeUTFBytes("}")
				} else {
					output.writeByte(printSetting.newLine)
					output.writeUTFBytes(currentIndent)
					output.writeUTFBytes("}")
				}
			} else {
				output.writeUTFBytes(printSetting.simpleFieldSeperator)
				const stringValue:String = value as String
				if (stringValue) {
					printString(output, stringValue)
				} else {
					const enumFieldDescriptor:FieldDescriptor$TYPE_ENUM =
							fieldDescriptor as FieldDescriptor$TYPE_ENUM
					if (enumFieldDescriptor) {
						printEnum(output, int(value),
								enumFieldDescriptor.enumType)
					} else {
						const enumRepeatedFieldDescriptor:
								RepeatedFieldDescriptor$TYPE_ENUM =
								fieldDescriptor as
								RepeatedFieldDescriptor$TYPE_ENUM
						if (enumRepeatedFieldDescriptor) {
							printEnum(output, int(value),
									enumRepeatedFieldDescriptor.enumType)
						} else {
							output.writeUTFBytes(value.toString())
						}
					}
				}
			}
		}
		
		/**
		 * Outputs a textual representation of the Protocol Message supplied into
		 * the parameter <code>output</code>.
		 */
		public static function printToUTFBytes(output:IDataOutput,
				message:Message,
				singleLineMode:Boolean = true):void {
			printMessageFields(output, message,
				singleLineMode ? SINGLELINE_MODE : MULTILINE_MODE)
		}
		
		/**
		 * Like <code>printToUTFBytes()</code>, but writes directly to a String and
		 * returns it.
		 */
		public static function printToString(message:Message,
				singleLineMode:Boolean = true):String {
			const ba:ByteArray = new ByteArray
			printToUTFBytes(ba, message, singleLineMode)
			ba.position = 0
			return ba.readUTFBytes(ba.length)
		}
		
		private static function skipWhitespace(source:ISource):void {
			for (;; ) {
				const b:int = source.read()
				switch (b) {
				case 0x20:/* space */
				case 0x09:/* \t */
				case 0x0a:/* \n */
				case 0x0d:/* \r */
					continue
				case 0x23:/* # */
					for (;;) {
						switch (source.read()) {
						case 0x0a:/* \n */
						case 0x0d:/* \r */
							break 
						}
					}
					break
				default:
					source.unread(b)
					return
				}
			}
		}
		private static function toHexDigit(b:int):int {
			if (b >= 0x30 && b <= 0x39) {
				return b - 0x30
			} else if (b >= 0x61 && b <= 0x66) {
				return b - 0x57
			} else if (b >= 0x41 && b <= 0x46) {
				return b - 0x37
			} else {
				throw new IOError("Expect hex, got " + String.fromCharCode(b))
			}
		}
		private static function toOctalDigit(b:int):int {
			if (b >= 0x30 && b <= 0x37) {
				return b - 0x30
			} else {
				throw new IOError("Expect digit, got " + String.fromCharCode(b))
			}
		}
		private static function tryConsumeBytes(source:ISource):ByteArray {
			skipWhitespace(source)
			const start:int = source.read()
			switch (start) {
			case 0x22 /* " */:
			case 0x27 /* ' */:
				const result:ByteArray = new ByteArray
				for (;;) {
					const b:int = source.read()
					switch (b) {
					case start:
						return result
					case 0x5c: /* \ */
						const b0:int = source.read()
						switch (b0) {
						case 0x61 /* \a */: result.writeByte(7); continue;
						case 0x62 /* \b */: result.writeByte(8); continue;
						case 0x66 /* \f */: result.writeByte(12); continue;
						case 0x6e /* \n */: result.writeByte(10); continue;
						case 0x72 /* \r */: result.writeByte(13); continue;
						case 0x74 /* \t */: result.writeByte(9); continue;
						case 0x76 /* \v */: result.writeByte(11); continue;
						case 0x78 /* \xXX */:
							const x0:int = source.read()
							const x1:int = source.read()
								result.writeByte(
										toHexDigit(x0) * 0x10 +
										toHexDigit(x1))
							continue
						default:
							if (b0 >= 0x30 && b0 <= 0x39) {
								const b1:int = source.read()
								const b2:int = source.read()
								result.writeByte(
										toOctalDigit(b0) * 64 +
										toOctalDigit(b1) * 8 +
										toOctalDigit(b2))
							} else {
								result.writeByte(b0)
							}
							continue
						}
					default:
						result.writeByte(b)
						break
					}
				}
				break
			default:
				source.unread(start)
				break
			}
			return null
		}
		
		private static function tryConsume(source:ISource,
				expected:int):Boolean {
			skipWhitespace(source)
			const b:int = source.read()
			if (b == expected) {
				return true
			} else {
				source.unread(b)
				return false
			}
		}
		
		private static function consume(source:ISource, expected:int):void {
			skipWhitespace(source)
			const b:int = source.read()
			if (b != expected) {
				throw new IOError("Expect " + String.fromCharCode(expected) + 
					", got " + String.fromCharCode(b))
			}
		}
		
		private static function consumeIdentifier(source:ISource):String {
			skipWhitespace(source)
			const nameBuffer:ByteArray = new ByteArray
			for (;; ) {
				const b:int = source.read()
				if (b >= 0x30 && b <= 0x39 || // 0-9
						b >= 0x41 && b <= 0x5a || // A-Z
						b >= 0x61 && b <= 0x7a || // a-z
						b == 0x2e || b == 0x5f || b == 0x2d || b < 0) {
					nameBuffer.writeByte(b)
				} else {
					if (nameBuffer.length == 0) {
						throw new IOError("Expect Identifier, got " +
								String.fromCharCode(b))
					}
					source.unread(b)
					break
				}
			}
			nameBuffer.position = 0
			return nameBuffer.readUTFBytes(nameBuffer.length)
		}
		
		private static function appendUnknown(message:Message, tag:uint,
				value:*):void {
			const oldValue:* = message[tag]
			if (oldValue === undefined) {
				message[tag] = value
			} else {
				const oldArray:Array = oldValue as Array
				if (oldArray) {
					oldArray.push(value)
				} else {
					message[tag] = [oldValue, value]
				}
			}
		}
		
		private static function consumeUnknown(source:ISource,
				message:Message, number:uint):void {
			const bytes:ByteArray = tryConsumeBytes(source)
			if (bytes) {
				appendUnknown(message,
						(number << 3) | WireType.LENGTH_DELIMITED,
						bytes)
				return
			}
			const identifier:String = consumeIdentifier(source)
			const m:Array = identifier.match(
					/^0[xX]([0-9a-fA-F]{16}|[0-9a-fA-F]{8})$/)
			if (!m) {
				appendUnknown(message,
						(number << 3) | WireType.VARINT,
						UInt64.parseUInt64(identifier))
				return
			}
			const hex:String = m[1]
			if (hex.length == 8) {
				appendUnknown(message,
						(number << 3) | WireType.FIXED_32_BIT,
						uint(parseInt(hex, 16)))
			} else {
				appendUnknown(message,
						(number << 3) | WireType.FIXED_64_BIT,
						UInt64.parseUInt64(hex, 16))
			}
		}
		
		private static function consumeEnumFieldValue(source:ISource,
				enumType:Class):int {
			consume(source, 0x3a/* : */)
			const enumName:String = consumeIdentifier(source)
			const result:* = enumType[enumName]
			if (result === undefined) {
				throw new IOError("Invalid enum name " + enumName)
			} else {
				return result
			}
		}
		
		private static function parseUnknown(message:Message):void {
			const buffer:WritingBuffer = new WritingBuffer
			for (var fieldName:String in message) {
				const tag:uint = uint(fieldName)
				if (tag == 0) {
					continue
				}
				WriteUtils.writeUnknownPair(buffer, tag, message[fieldName])
				delete message[fieldName]
			}
			const normalBuffer:ByteArray = new ByteArray
			buffer.toNormal(normalBuffer)
			normalBuffer.position = 0
			message.mergeFrom(normalBuffer)
		}
		
		private static function consumeFieldValue(source:ISource,
				type:Class):* {
			switch (type) {
				case ByteArray:
					consume(source, 0x3a/* : */)
					const bytes:ByteArray = tryConsumeBytes(source)
					if (bytes) {
						bytes.position = 0
						return bytes
					} else {
						throw new IOError("Expect quoted bytes")
					}
				case String:
					consume(source, 0x3a/* : */)
					const binaryString:ByteArray = tryConsumeBytes(source)
					if (binaryString) {
						binaryString.position = 0
						return binaryString.readUTFBytes(binaryString.length)
					} else {
						throw new IOError("Expect quoted string")
					}
				case Boolean:
					consume(source, 0x3a/* : */)
					const booleanString:String = consumeIdentifier(source)
					switch (booleanString) {
					case "true":
						return true
					case "false":
						return false
					default:
						throw new IOError("Expect boolean, got " +
								booleanString)
					}
					break
				case Int64:
					consume(source, 0x3a/* : */)
					return Int64.parseInt64(consumeIdentifier(source))
				case UInt64:
					consume(source, 0x3a/* : */)
					return UInt64.parseUInt64(consumeIdentifier(source))
				case uint:
					consume(source, 0x3a/* : */)
					return uint(parseInt(consumeIdentifier(source)))
				case int:
					consume(source, 0x3a/* : */)
					return int(parseInt(consumeIdentifier(source)))
				case Number:
					consume(source, 0x3a/* : */)
					return parseFloat(consumeIdentifier(source))
				default:
					tryConsume(source, 0x3a/* : */)
					consume(source, 0x7b/* { */)
					const message:Message = new type
					for (;; ) {
						if (tryConsume(source, 0x7d/* } */)) {
							break
						}
						consumeField(source, message)
					}
					parseUnknown(message)
					return message
			}
		}
		
		private static function consumeField(source:ISource,
				message:Message):void {
			const isExtension:Boolean = tryConsume(source, 0x5b /* [ */)
			const name:String = consumeIdentifier(source)
			if (isExtension) {
				consume(source, 0x5d /* ] */)
			}
			var fieldDescriptor:BaseFieldDescriptor
			if (isExtension) {
				const lastDotPosition:int = name.lastIndexOf('.')
				const scope:String = name.substring(0, lastDotPosition)
				const localName:String = name.substring(lastDotPosition + 1)
				try {
					fieldDescriptor = getDefinitionByName(scope)[
							localName.toUpperCase()]
				} catch (e:ReferenceError) {
					try {
						fieldDescriptor = BaseFieldDescriptor(
								getDefinitionByName(scope + '.' +
								localName.toUpperCase()))
					} catch (e:ReferenceError) {
						throw new IOError("Unknown extension: " + name)
					}
				}
			} else {
				if (name.search(/[0-9]+/) == 0) {
					consume(source, 0x3a/* : */)
					consumeUnknown(source, message, uint(name))
					return
				} else {
					fieldDescriptor = Object(message).constructor[
							name.toUpperCase()]
				}
			}
			const repeatedFieldDescriptor:RepeatedFieldDescriptor =
					fieldDescriptor as RepeatedFieldDescriptor
			if (repeatedFieldDescriptor) {
				const destination:Array =
						message[fieldDescriptor.name] ||
						(message[fieldDescriptor.name] = [])
				const enumRepeatedFieldDescriptor:
						RepeatedFieldDescriptor$TYPE_ENUM =
						repeatedFieldDescriptor as
						RepeatedFieldDescriptor$TYPE_ENUM
				destination.push(enumRepeatedFieldDescriptor ?
						consumeEnumFieldValue(source,
						enumRepeatedFieldDescriptor.enumType) :
						consumeFieldValue(source,
						repeatedFieldDescriptor.elementType))
			} else {
				const enumFieldDescriptor:FieldDescriptor$TYPE_ENUM =
						fieldDescriptor as FieldDescriptor$TYPE_ENUM
				message[fieldDescriptor.name] = enumFieldDescriptor ?
						consumeEnumFieldValue(source,
						enumFieldDescriptor.enumType) :
						consumeFieldValue(source,
						fieldDescriptor.type)
			}
		}
		
		private static function mergeFromSource(source:ISource,
				message:Message):void {
			for (;; ) {
				if (tryConsume(source, 0/* EOF */)) {
					break
				}
				consumeField(source, message)
			}
			parseUnknown(message)
		}
		
		/**
		 * Parse a text-format message from <code>input</code> and merge the
		 * contents into <code>message</code>.
		 */
		public static function mergeFromUTFBytes(input:IDataInput,
				message:Message):void {
			mergeFromSource(new WrappedSource(input), message)
		}
		
		/**
		 * Parse a text-format message from <code>text</code> and merge the
		 * contents into <code>message</code>.
		 */
		public static function mergeFromString(text:String, message:Message):void {
			const source:BufferedSource = new BufferedSource
			source.writeUTFBytes(text)
			source.position = 0
			mergeFromSource(source, message)
		}
	}
}
import flash.errors.IOError;
import flash.utils.IDataInput;
import flash.utils.ByteArray;
import flash.errors.EOFError


interface ISource {
	function read():int
	function unread(b:int):void
}

class BufferedSource extends ByteArray implements ISource {
	public function unread(value:int):void {
		if (value == 0 && bytesAvailable == 0) {
			return
		}
		position--
	}
	public function read():int {
		if (bytesAvailable > 0) {
			return readByte()
		} else {
			return 0
		}
	}
}

class WrappedSource implements ISource {
	private var input:IDataInput
	private var temp:int
	public function WrappedSource(input:IDataInput) {
		this.input = input
	}
	public function unread(value:int):void {
		if (temp) {
			throw new IOError("Cannot unread twice!")
		}
		temp = value
	}
	public function read():int {
		if (temp) {
			const result:int = temp
			temp = 0
			return result
		} else {
			try {
				return input.readByte()
			} catch (e: EOFError) {
			}
			return 0
		}
	}
}

class PrintSetting {
	public var newLine:uint
	public var indentChars:String
	public var simpleFieldSeperator:String
}

const SINGLELINE_MODE:PrintSetting = new PrintSetting
SINGLELINE_MODE.newLine = ' '.charCodeAt()
SINGLELINE_MODE.indentChars = ""
SINGLELINE_MODE.simpleFieldSeperator = ":"

const MULTILINE_MODE:PrintSetting = new PrintSetting
MULTILINE_MODE.newLine = '\n'.charCodeAt()
MULTILINE_MODE.indentChars = "  "
MULTILINE_MODE.simpleFieldSeperator = ": "
