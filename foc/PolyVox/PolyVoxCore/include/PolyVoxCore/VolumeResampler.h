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

#ifndef __PolyVox_VolumeResampler_H__
#define __PolyVox_VolumeResampler_H__

#include <cmath>

namespace PolyVox
{
	template< typename SrcVolumeType, typename DstVolumeType>
	class VolumeResampler
	{
	public:
		VolumeResampler(SrcVolumeType* pVolSrc, Region regSrc, DstVolumeType* pVolDst, Region regDst);

		void execute();

	private:
		void resampleSameSize();
		void resampleArbitrary();

		//Source data
		SrcVolumeType* m_pVolSrc;
		Region m_regSrc;

		//Destination data
		DstVolumeType* m_pVolDst;
		Region m_regDst;
	};

}//namespace PolyVox

#include "PolyVoxCore/VolumeResampler.inl"

#endif

