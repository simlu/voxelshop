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

#ifndef __PolyVox_ArraySizesImpl_H__
#define __PolyVox_ArraySizesImpl_H__

#include "PolyVoxImpl/TypeDef.h"

namespace PolyVox
{
	/*
	This class provides the implementation details behind ArraySizes. It is actually
	quite similar to ArraySizes, but an important difference is that it is templatised
	whereas ArraySizes is not. This allows us to use a recursive template pattern without
	exposing the use of templates to the user.

	It is based on the following article: http://www.drdobbs.com/cpp/184401319
	*/
	template <uint32_t N>
	class ArraySizesImpl
	{
		typedef const uint32_t (&UIntArrayN)[N];

		friend class ArraySizes;
		friend class ArraySizesImpl<N-1>;

	public:
		ArraySizesImpl<N+1> operator () (uint32_t uSize);

		operator UIntArrayN () const;

	private:
		ArraySizesImpl(const uint32_t (&pSizes)[N-1], uint32_t uSize);

		uint32_t m_pSizes[N];
	};
}//namespace PolyVox

#include "PolyVoxImpl/ArraySizesImpl.inl"

#endif //__PolyVox_ArraySizesImpl_H__
