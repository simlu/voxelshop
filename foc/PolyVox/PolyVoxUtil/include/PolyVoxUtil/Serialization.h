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

#ifndef __PolyVox_Serialization_H__
#define __PolyVox_Serialization_H__

#include "PolyVoxImpl/Utility.h"

#include "PolyVoxCore/Region.h"

#include <iostream>
#include <memory>

namespace PolyVox
{	
	class VolumeSerializationProgressListener
	{
	public:
		virtual void onProgressUpdated(float fProgress) = 0;
	};

	////////////////////////////////////////////////////////////////////////////////
	// THESE FUNCTIONS ARE DEPRECATED. USE VERSIONED 'loadVolume' AND 'saveVolume' INSTEAD.
	////////////////////////////////////////////////////////////////////////////////
	template< typename VolumeType >
	polyvox_shared_ptr< VolumeType > loadVolumeRaw(std::istream& stream, VolumeSerializationProgressListener* progressListener = 0);
	template< typename VolumeType >
	void saveVolumeRaw(std::ostream& stream, VolumeType& volume, VolumeSerializationProgressListener* progressListener = 0);

	template< typename VolumeType >
	polyvox_shared_ptr< VolumeType > loadVolumeRle(std::istream& stream, VolumeSerializationProgressListener* progressListener = 0);
	template< typename VolumeType >
	void saveVolumeRle(std::ostream& stream, VolumeType& volume, VolumeSerializationProgressListener* progressListener = 0);

	////////////////////////////////////////////////////////////////////////////////
	// END OF DEPRECATED FUNCTIONS
	////////////////////////////////////////////////////////////////////////////////

	template< typename VolumeType >
	bool loadVolume(std::istream& stream, VolumeType& volume, VolumeSerializationProgressListener* progressListener = 0);
	template< typename VolumeType >
	bool saveVolume(std::ostream& stream, VolumeType& volume, VolumeSerializationProgressListener* progressListener = 0);

	template< typename VolumeType >
	bool loadVersion0(std::istream& stream, VolumeType& volume, VolumeSerializationProgressListener* progressListener = 0);
	template< typename VolumeType >
	bool saveVersion0(std::ostream& stream, VolumeType& volume, VolumeSerializationProgressListener* progressListener = 0);
}

#include "PolyVoxUtil/Serialization.inl"

#endif
