// vim: tabstop=4 shiftwidth=4

// Copyright (c) 2011 , Yang Bo All rights reserved.
//
// Author: Yang Bo (pop.atry@gmail.com)
//
// Use, modification and distribution are subject to the "New BSD License"
// as listed at <url: http://www.opensource.org/licenses/bsd-license.php >.

package com.netease.protobuf {
	public final class UInt64 extends Binary64 {
		public final function set high(value:uint):void {
			internalHigh = value
		}
		public final function get high():uint {
			return internalHigh
		}
		public function UInt64(low:uint = 0, high:uint = 0) {
			super(low, high)
		}
		/**
		 * Convert from <code>Number</code>.
		 */
		public static function fromNumber(n: Number):UInt64 {
			return new UInt64(n, Math.floor(n / 4294967296.0))
		}
		/**
		 * Convert to <code>Number</code>.
		 */
		public final function toNumber():Number {
			return (high * 4294967296) + low
		}
    	public final function toString(radix:uint = 10):String {
			if (radix < 2 || radix > 36) {
				throw new ArgumentError
			}
			if (high == 0) {
				return low.toString(radix)
			}
			const digitChars:Array = [];
			const copyOfThis:UInt64 = new UInt64(low, high);
			do {
				const digit:uint = copyOfThis.div(radix);
				digitChars.push((digit < 10 ? '0' : 'a').charCodeAt() + digit)
			} while (copyOfThis.high != 0)
			return copyOfThis.low.toString(radix) +
					String.fromCharCode.apply(
					String, digitChars.reverse())
		}
		public static function parseUInt64(str:String, radix:uint = 0):UInt64 {
			var i:uint = 0
			if (radix == 0) {
				if (str.search(/^0x/) == 0) {
					radix = 16
					i = 2
				} else {
					radix = 10
				}
			}
			if (radix < 2 || radix > 36) {
				throw new ArgumentError
			}
			str = str.toLowerCase()
			const result:UInt64 = new UInt64
			for (; i < str.length; i++) {
				var digit:uint = str.charCodeAt(i)
				if (digit >= '0'.charCodeAt() && digit <= '9'.charCodeAt()) {
					digit -= '0'.charCodeAt()
				} else if (digit >= 'a'.charCodeAt() && digit <= 'z'.charCodeAt()) {
					digit -= 'a'.charCodeAt()
				} else {
					throw new ArgumentError
				}
				if (digit >= radix) {
					throw new ArgumentError
				}
				result.mul(radix)
				result.add(digit)
			}
			return result
		}
	}
}
