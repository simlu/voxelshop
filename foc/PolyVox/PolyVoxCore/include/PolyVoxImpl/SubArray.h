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

#ifndef __PolyVox_SubArray_H__
#define __PolyVox_SubArray_H__

#include "PolyVoxImpl/TypeDef.h"

namespace PolyVox
{
	template <uint32_t noOfDims, typename ElementType> class Array;

	/*
	This class forms part of the implementation of the Array class. The operator[]
	return a SubArray of the next size down, so that multiple []'s can be chained
	together. It is a seperate class from Array so that it can have a reduced interface,
	and also so that it never takes ownership of the memory to which it points.

	It is based on the following article: http://www.drdobbs.com/cpp/184401319
	*/
	template <uint32_t noOfDims, typename ElementType>
	class SubArray
	{
		friend class Array<noOfDims+1, ElementType>;
		friend class SubArray<noOfDims+1, ElementType>;

	public:
		SubArray<noOfDims-1, ElementType> operator [](uint32_t uIndex);

		const SubArray<noOfDims-1, ElementType> operator [](uint32_t uIndex) const;

	private:
		SubArray<noOfDims, ElementType>(ElementType * pElements, uint32_t * pDimensions, uint32_t * pOffsets);

		uint32_t * m_pDimensions;
		uint32_t * m_pOffsets;
		uint32_t m_uNoOfElements;
		ElementType * m_pElements;
	};

	template <typename ElementType>
	class SubArray<1, ElementType>
	{
		friend class Array<2, ElementType>;
		friend class SubArray<2, ElementType>;

	public:
		ElementType & operator [] (uint32_t uIndex);

		const ElementType & operator [] (uint32_t uIndex) const;

	private:
		SubArray<1, ElementType>(ElementType * pElements, uint32_t * pDimensions, uint32_t * /*pOffsets*/);

		uint32_t * m_pDimensions;
		ElementType * m_pElements;
	};

	template <typename ElementType>
	class SubArray<0, ElementType>
	{
		//Zero dimensional subarray is meaningless.
	};
}//namespace PolyVox

#include "PolyVoxImpl/SubArray.inl"

#endif //__PolyVox_SubArray_H__
