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

#ifndef __PolyVox_VolumeChangeTracker_H__
#define __PolyVox_VolumeChangeTracker_H__

#include "PolyVoxImpl/Utility.h"

#include "PolyVoxCore/Region.h"
#include "PolyVoxCore/SurfaceMesh.h"
#include "PolyVoxCore/Vector.h"

namespace PolyVox
{	
	/// Voxel scene manager
	template <typename VoxelType>
	class VolumeChangeTracker
	{
	public:
		//Constructors, etc
		VolumeChangeTracker(LargeVolume<VoxelType>* volumeDataToSet, uint16_t regionSideLength);
		~VolumeChangeTracker();

		//Getters
		int32_t getCurrentTime(void) const;	
		int32_t getLastModifiedTimeForRegion(uint16_t uX, uint16_t uY, uint16_t uZ);
		LargeVolume<VoxelType>* getWrappedVolume(void) const;

		//Setters
		void setAllRegionsModified(void);
		void setLockedVoxelAt(uint16_t x, uint16_t y, uint16_t z, VoxelType value);
		void setVoxelAt(uint16_t x, uint16_t y, uint16_t z, VoxelType value);

		//Others	
		void lockRegion(const Region& regToLock);
		void unlockRegion(void);
		//void markRegionChanged(uint16_t firstX, uint16_t firstY, uint16_t firstZ, uint16_t lastX, uint16_t lastY, uint16_t lastZ);

	public:
		void incrementCurrentTime(void);
		bool m_bIsLocked;
		Region m_regLastLocked;
		LargeVolume<VoxelType>* volumeData;

		uint16_t m_uRegionSideLength;
		uint8_t m_uRegionSideLengthPower;
		uint16_t m_uVolumeWidthInRegions;
		uint16_t m_uVolumeHeightInRegions;
		uint16_t m_uVolumeDepthInRegions;


		//It's not what the block class was designed for, but it 
		//provides a handy way of storing a 3D grid of values.
		LargeVolume<int32_t>* volRegionLastModified;

		static uint32_t m_uCurrentTime;
	};
}

#include "PolyVoxUtil/VolumeChangeTracker.inl"

#endif
