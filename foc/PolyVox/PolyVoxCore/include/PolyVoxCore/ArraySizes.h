/*******************************************************************************
Copyright (c) 2005-2009 David Williams

This software is provided 'as-is', without any express or implied
warranty. In no event will the authors be held liable for any damages
arising from the use of this software.

Permission is granted to anyone to use this software for any purpose,
including commercial applications, and to alter it and redistribute it
freely, subject to the following restrictions:

1. The origin of this software must not be misrepresented; you must not
claim that you wrote the original software. If you use this software
in a product, an acknowledgment in the product documentation would be
appreciated but is not required.

2. Altered source versions must be plainly marked as such, and must not be
misrepresented as being the original software.

3. This notice may not be removed or altered from any source
distribution. 	
*******************************************************************************/

#ifndef __PolyVox_ArraySizes_H__
#define __PolyVox_ArraySizes_H__

#include "PolyVoxImpl/ArraySizesImpl.h"
#include "PolyVoxImpl/TypeDef.h"

namespace PolyVox
{
	///The ArraySizes class provide a convienient way to specify the dimensions of an Array.
	////////////////////////////////////////////////////////////////////////////////
	/// The Array class requires an array of integers to be passed to the constructor 
	/// to specify the dimensions of the Array to be built. C++ does not allow this to
	/// be done in place, and so it typically requires an extra line of code - something
	/// like this:
	///
	/// \code
	/// uint32_t dimensions[3] = {10, 20, 30}; // Array dimensions
	/// Array<3,float> array(dimensions);
	/// \endcode
	///
	/// The ArraySizes class can be constructed in place, and also provides implicit 
	/// conversion to an array of integers. Hence it is now possible to declare the
	/// above Array as follows:
	///
	/// \code
	/// Array<3,float> array(ArraySizes(10)(20)(30));
	/// \endcode
	///
	/// Usage of this class is therefore very simple, although the template code 
	/// behind it may appear complex. For reference, it is based upon the article here:
	/// http://www.drdobbs.com/cpp/184401319/
	////////////////////////////////////////////////////////////////////////////////
	class POLYVOX_API ArraySizes
	{
		typedef const uint32_t (&UIntArray1)[1];

	public:
		/// Constructor
		explicit ArraySizes(uint32_t uSize);

		/// Duplicates this object but with an extra dimension
		ArraySizesImpl<2> operator () (uint32_t uSize);

		/// Converts this object to an array of integers
		operator UIntArray1 () const;

	private:
		// This class is only one dimensional. Higher dimensions 
		// are implemented via the ArraySizesImpl class.
		uint32_t m_pSizes[1]; 
	};
}//namespace PolyVox

#endif //__PolyVox_ArraySizes_H__
