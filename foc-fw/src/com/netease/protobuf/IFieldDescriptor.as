// vim: tabstop=4 shiftwidth=4

// Copyright (c) 2011 , Yang Bo. All rights reserved.
//
// Author: Yang Bo (pop.atry@gmail.com)
//
// Use, modification and distribution are subject to the New BSD License
// as listed at <url: http://www.opensource.org/licenses/bsd-license.php >.

package com.netease.protobuf {
	/**
	 * Run-time infomation for a field.
	 */
	public interface IFieldDescriptor {
		function get name():String

		function get tagNumber():uint
	}
}
