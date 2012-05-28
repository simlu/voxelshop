// vim: tabstop=4 shiftwidth=4

// Copyright (c) 2011 , Yang Bo All rights reserved.
//
// Author: Yang Bo (pop.atry@gmail.com)
//
// Use, modification and distribution are subject to the "New BSD License"
// as listed at <url: http://www.opensource.org/licenses/bsd-license.php >.

package com.netease.protobuf {
	public final class Int64 extends Binary64 {
		public final function set high(value:int):void {
			internalHigh = value
		}
		public final function get high():int {
			return internalHigh
		}
		public function Int64(low:uint = 0, high:int = 0) {
			super(low, high)
		}
		/**
		 * Convert from <code>Number</code>.
		 */
		public static function fromNumber(n: Number):Int64 {
			return new Int64(n, Math.floor(n / 4294967296.0))
		}
		/**
		 * Convert to <code>Number</code>.
		 */
		public final function toNumber():Number {
			return high * 4294967296 + low
		}
		public final function toString(radix:uint = 10):String {
			if (radix < 2 || radix > 36) {
				throw new ArgumentError
			}
			switch (high) {
				case 0:
				{
					return low.toString(radix)
				}

				case -1:
				{
					return int(low).toString(radix)
				}

				default:
				{
					break;
				}
			}
			if (low == 0 && high == 0) {
				return "0"
			}
			const digitChars:Array = [];
			const copyOfThis:UInt64 = new UInt64(low, high);
			if (high < 0) {
				copyOfThis.bitwiseNot()
				copyOfThis.add(1)
			}
			do {
				const digit:uint = copyOfThis.div(radix);
				digitChars.push((digit < 10 ? '0' : 'a').charCodeAt() + digit)
			} while (copyOfThis.high != 0)
			if (high < 0) {
				return '-' + copyOfThis.low.toString(radix) +
						String.fromCharCode.apply(
						String, digitChars.reverse())
			} else {
				return copyOfThis.low.toString(radix) +
						String.fromCharCode.apply(
						String, digitChars.reverse())
			}
		}
		public static function parseInt64(str:String, radix:uint = 0):Int64 {
			const negative:Boolean = str.search(/^\-/) == 0
			var i:uint = negative ? 1 : 0
			if (radix == 0) {
				if (str.search(/^\-?0x/) == 0) {
					radix = 16
					i += 2
				} else {
					radix = 10
				}
			}
			if (radix < 2 || radix > 36) {
				throw new ArgumentError
			}
			str = str.toLowerCase()
			const result:Int64 = new Int64
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
			if (negative) {
				result.bitwiseNot()
				result.add(1)
			}
			return result
		}
	}
}
