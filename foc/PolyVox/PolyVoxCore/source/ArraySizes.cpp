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

#include "PolyVoxCore/ArraySizes.h"

namespace PolyVox
{	
	/**
    \param uSize The size of the first dimension.
    */
	ArraySizes::ArraySizes(uint32_t uSize) 
	{ 
		m_pSizes[0]=uSize;
	}

	/**
    This class only directly implements one dimensional sizes. Higher numbers
	of dimensions are implemented via the ArraySisesImpl class. This function
	create an object of the next dimensionality up.
    \param uSize The size of the next dimension.
    \return A higher dimension version of this class.
    */
	ArraySizesImpl<2> ArraySizes::operator () (uint32_t uSize) 
	{ 
		return ArraySizesImpl<2>(m_pSizes, uSize);
	}

	/**
    \return The array of integers corresponding to this object.
    */
	ArraySizes::operator UIntArray1 () const
	{
		return m_pSizes;
	}
}
