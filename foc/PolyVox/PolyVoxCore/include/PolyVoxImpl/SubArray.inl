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

#include <cassert>

namespace PolyVox
{
	template <uint32_t noOfDims, typename ElementType>
	SubArray<noOfDims-1, ElementType> SubArray<noOfDims, ElementType>::operator[](uint32_t uIndex)
	{
		assert(uIndex<m_pDimensions[0]);
		return
			SubArray<noOfDims-1, ElementType>(&m_pElements[uIndex*m_pOffsets[0]],
			m_pDimensions+1, m_pOffsets+1);
	}

	template <uint32_t noOfDims, typename ElementType>
	const SubArray<noOfDims-1, ElementType> SubArray<noOfDims, ElementType>::operator[](uint32_t uIndex) const
	{
		assert(uIndex<m_pDimensions[0]);
		return
			SubArray<noOfDims-1, ElementType>(&m_pElements[uIndex*m_pOffsets[0]],
			m_pDimensions+1, m_pOffsets+1);
	}

	template <uint32_t noOfDims, typename ElementType>
	SubArray<noOfDims, ElementType>::SubArray(ElementType * pElements, uint32_t * pDimensions, uint32_t * pOffsets)
		:m_pDimensions(pDimensions)
		,m_pOffsets(pOffsets)
		,m_uNoOfElements(0)
		,m_pElements(pElements)
	{
	} 


	template <typename ElementType>
	ElementType& SubArray<1, ElementType>::operator[] (uint32_t uIndex)
	{
		assert(uIndex<m_pDimensions[0]);
		return m_pElements[uIndex];
	}

	template <typename ElementType>
	const ElementType& SubArray<1, ElementType>::operator[] (uint32_t uIndex) const
	{
		assert(uIndex<m_pDimensions[0]);
		return m_pElements[uIndex];
	}

	template <typename ElementType>
	SubArray<1, ElementType>::SubArray(ElementType * pElements, uint32_t * pDimensions, uint32_t * /*pOffsets*/)
		:m_pDimensions(pDimensions)
		,m_pElements(pElements)			
	{
	}
}//namespace PolyVox
