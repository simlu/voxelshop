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

namespace PolyVox
{
	template <typename VoxelType>
	uint32_t VolumeChangeTracker<VoxelType>::m_uCurrentTime = 0;

	//////////////////////////////////////////////////////////////////////////
	// VolumeChangeTracker
	//////////////////////////////////////////////////////////////////////////
	template <typename VoxelType>
	VolumeChangeTracker<VoxelType>::VolumeChangeTracker(LargeVolume<VoxelType>* volumeDataToSet, uint16_t regionSideLength)
		:m_bIsLocked(false)
		,volumeData(0)
		,m_uRegionSideLength(regionSideLength)
	{	
		volumeData = volumeDataToSet;
		m_uVolumeWidthInRegions = volumeData->getWidth() / m_uRegionSideLength;
		m_uVolumeHeightInRegions = volumeData->getHeight() / m_uRegionSideLength;
		m_uVolumeDepthInRegions = volumeData->getDepth() / m_uRegionSideLength;
		m_uRegionSideLengthPower = PolyVox::logBase2(m_uRegionSideLength);

		volRegionLastModified = new LargeVolume<int32_t>(m_uVolumeWidthInRegions, m_uVolumeHeightInRegions, m_uVolumeDepthInRegions, 0);
	}

	template <typename VoxelType>
	VolumeChangeTracker<VoxelType>::~VolumeChangeTracker()
	{
	}

	template <typename VoxelType>
	void VolumeChangeTracker<VoxelType>::setAllRegionsModified(void)
	{
		incrementCurrentTime();
		for(uint16_t blockZ = 0; blockZ < m_uVolumeDepthInRegions; ++blockZ)
		{
			for(uint16_t blockY = 0; blockY < m_uVolumeHeightInRegions; ++blockY)
			{
				for(uint16_t blockX = 0; blockX < m_uVolumeWidthInRegions; ++blockX)
				{
					volRegionLastModified->setVoxelAt(blockX, blockY, blockZ, m_uCurrentTime);					
				}
			}
		}
	}

	template <typename VoxelType>
	int32_t VolumeChangeTracker<VoxelType>::getCurrentTime(void) const
	{
		return m_uCurrentTime;
	}

	template <typename VoxelType>
	int32_t VolumeChangeTracker<VoxelType>::getLastModifiedTimeForRegion(uint16_t uX, uint16_t uY, uint16_t uZ)
	{
		return volRegionLastModified->getVoxelAt(uX, uY, uZ);
	}

	template <typename VoxelType>
	LargeVolume<VoxelType>* VolumeChangeTracker<VoxelType>::getWrappedVolume(void) const
	{
		return volumeData;
	}

	template <typename VoxelType>
	void VolumeChangeTracker<VoxelType>::setVoxelAt(uint16_t x, uint16_t y, uint16_t z, VoxelType value)
	{
		//Note: We increase the time stamp both at the start and the end
		//to avoid ambiguity about whether the timestamp comparison should
		//be '<' vs '<=' or '>' vs '>=' in the users code.
		incrementCurrentTime();

		volumeData->setVoxelAt(x,y,z,value);
		
		//If we are not on a boundary, just mark one region.
		if((x % m_uRegionSideLength != 0) &&
			(x % m_uRegionSideLength != m_uRegionSideLength-1) &&
			(y % m_uRegionSideLength != 0) &&
			(y % m_uRegionSideLength != m_uRegionSideLength-1) &&
			(z % m_uRegionSideLength != 0) &&
			(z % m_uRegionSideLength != m_uRegionSideLength-1))
		{
			volRegionLastModified->setVoxelAt(x >> m_uRegionSideLengthPower, y >> m_uRegionSideLengthPower, z >> m_uRegionSideLengthPower, m_uCurrentTime);
		}
		else //Mark surrounding regions as well
		{
			const uint16_t regionX = x >> m_uRegionSideLengthPower;
			const uint16_t regionY = y >> m_uRegionSideLengthPower;
			const uint16_t regionZ = z >> m_uRegionSideLengthPower;

			const uint16_t minRegionX = (std::max)(uint16_t(0),uint16_t(regionX-1));
			const uint16_t minRegionY = (std::max)(uint16_t(0),uint16_t(regionY-1));
			const uint16_t minRegionZ = (std::max)(uint16_t(0),uint16_t(regionZ-1));

			const uint16_t maxRegionX = (std::min)(uint16_t(m_uVolumeWidthInRegions-1),uint16_t(regionX+1));
			const uint16_t maxRegionY = (std::min)(uint16_t(m_uVolumeHeightInRegions-1),uint16_t(regionY+1));
			const uint16_t maxRegionZ = (std::min)(uint16_t(m_uVolumeDepthInRegions-1),uint16_t(regionZ+1));

			for(uint16_t zCt = minRegionZ; zCt <= maxRegionZ; zCt++)
			{
				for(uint16_t yCt = minRegionY; yCt <= maxRegionY; yCt++)
				{
					for(uint16_t xCt = minRegionX; xCt <= maxRegionX; xCt++)
					{
						volRegionLastModified->setVoxelAt(xCt,yCt,zCt,m_uCurrentTime);
					}
				}
			}
		}

		//Increment time stamp. See earlier note.
		incrementCurrentTime();
	}

	template <typename VoxelType>
	void VolumeChangeTracker<VoxelType>::setLockedVoxelAt(uint16_t x, uint16_t y, uint16_t z, VoxelType value)
	{
		assert(m_bIsLocked);

		//FIXME - rather than creating a iterator each time we should have one stored
		/*Sampler<VoxelType> iterVol(*volumeData);
		iterVol.setPosition(x,y,z);
		iterVol.setVoxel(value);*/
		volumeData->setVoxelAt(x,y,z,value);
	}

	template <typename VoxelType>
	void VolumeChangeTracker<VoxelType>::lockRegion(const Region& regToLock)
	{
		if(m_bIsLocked)
		{
			throw std::logic_error("A region is already locked. Please unlock it before locking another.");
		}

		m_regLastLocked = regToLock;
		m_bIsLocked = true;
	}

	template <typename VoxelType>
	void VolumeChangeTracker<VoxelType>::unlockRegion(void)
	{
		if(!m_bIsLocked)
		{
			throw std::logic_error("No region is locked. You must lock a region before you can unlock it.");
		}

		//Note: We increase the time stamp both at the start and the end
		//to avoid ambiguity about whether the timestamp comparison should
		//be '<' vs '<=' or '>' vs '>=' in the users code.
		incrementCurrentTime();

		const uint16_t firstRegionX = m_regLastLocked.getLowerCorner().getX() >> m_uRegionSideLengthPower;
		const uint16_t firstRegionY = m_regLastLocked.getLowerCorner().getY() >> m_uRegionSideLengthPower;
		const uint16_t firstRegionZ = m_regLastLocked.getLowerCorner().getZ() >> m_uRegionSideLengthPower;

		const uint16_t lastRegionX = m_regLastLocked.getUpperCorner().getX() >> m_uRegionSideLengthPower;
		const uint16_t lastRegionY = m_regLastLocked.getUpperCorner().getY() >> m_uRegionSideLengthPower;
		const uint16_t lastRegionZ = m_regLastLocked.getUpperCorner().getZ() >> m_uRegionSideLengthPower;

		for(uint16_t zCt = firstRegionZ; zCt <= lastRegionZ; zCt++)
		{
			for(uint16_t yCt = firstRegionY; yCt <= lastRegionY; yCt++)
			{
				for(uint16_t xCt = firstRegionX; xCt <= lastRegionX; xCt++)
				{
					volRegionLastModified->setVoxelAt(xCt,yCt,zCt,m_uCurrentTime);
				}
			}
		}

		m_bIsLocked = false;

		//Increment time stamp. See earlier note.
		incrementCurrentTime();
	}

	template <typename VoxelType>
	void VolumeChangeTracker<VoxelType>::incrementCurrentTime(void)
	{
		//Increment the current time.
		uint32_t time = m_uCurrentTime++;

		//Watch out for wraparound. Hopefully this will never happen
		//as we have a pretty big counter, but it's best to be sure...
		assert(time < m_uCurrentTime);
		if(time >= m_uCurrentTime)
		{
			throw std::overflow_error("The VolumeChangeTracker time has overflowed.");
		}
	}
}
