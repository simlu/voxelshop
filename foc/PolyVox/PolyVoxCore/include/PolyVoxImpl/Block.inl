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

#include "PolyVoxImpl/Utility.h"
#include "PolyVoxCore/Vector.h"

#include <cassert>
#include <cstring> //For memcpy
#include <limits>
#include <stdexcept> //for std::invalid_argument

namespace PolyVox
{
	template <typename VoxelType>
	Block<VoxelType>::Block(uint16_t uSideLength)
		:m_tUncompressedData(0)
		,m_uSideLength(0)
		,m_uSideLengthPower(0)
		,m_bIsCompressed(true)
		,m_bIsUncompressedDataModified(true)
	{
		if(uSideLength != 0)
		{
			initialise(uSideLength);
		}
	}

	template <typename VoxelType>
	uint16_t Block<VoxelType>::getSideLength(void) const
	{
		return m_uSideLength;
	}

	template <typename VoxelType>
	VoxelType Block<VoxelType>::getVoxelAt(uint16_t uXPos, uint16_t uYPos, uint16_t uZPos) const
	{
		assert(uXPos < m_uSideLength);
		assert(uYPos < m_uSideLength);
		assert(uZPos < m_uSideLength);

		assert(m_tUncompressedData);

		return m_tUncompressedData
			[
				uXPos + 
				uYPos * m_uSideLength + 
				uZPos * m_uSideLength * m_uSideLength
			];
	}

	template <typename VoxelType>
	VoxelType Block<VoxelType>::getVoxelAt(const Vector3DUint16& v3dPos) const
	{
		return getVoxelAt(v3dPos.getX(), v3dPos.getY(), v3dPos.getZ());
	}

	template <typename VoxelType>
	void Block<VoxelType>::setVoxelAt(uint16_t uXPos, uint16_t uYPos, uint16_t uZPos, VoxelType tValue)
	{
		assert(uXPos < m_uSideLength);
		assert(uYPos < m_uSideLength);
		assert(uZPos < m_uSideLength);

		assert(m_tUncompressedData);

		m_tUncompressedData
		[
			uXPos + 
			uYPos * m_uSideLength + 
			uZPos * m_uSideLength * m_uSideLength
		] = tValue;

		m_bIsUncompressedDataModified = true;
	}

	template <typename VoxelType>
	void Block<VoxelType>::setVoxelAt(const Vector3DUint16& v3dPos, VoxelType tValue)
	{
		setVoxelAt(v3dPos.getX(), v3dPos.getY(), v3dPos.getZ(), tValue);
	}

	template <typename VoxelType>
	void Block<VoxelType>::fill(VoxelType tValue)
	{
		if(!m_bIsCompressed)
		{
			//The memset *may* be faster than the std::fill(), but it doesn't compile nicely
			//in 64-bit mode as casting the pointer to an int causes a loss of precision.
			const uint32_t uNoOfVoxels = m_uSideLength * m_uSideLength * m_uSideLength;
			std::fill(m_tUncompressedData, m_tUncompressedData + uNoOfVoxels, tValue);

			m_bIsUncompressedDataModified = true;
		} 
		else
		{
			RunlengthEntry<uint16_t> rle;
			rle.length = m_uSideLength*m_uSideLength*m_uSideLength;
			rle.value = tValue;
			m_vecCompressedData.clear();
			m_vecCompressedData.push_back(rle);
		}
	}

	template <typename VoxelType>
	void Block<VoxelType>::initialise(uint16_t uSideLength)
	{
		//Debug mode validation
		assert(isPowerOf2(uSideLength));

		//Release mode validation
		if(!isPowerOf2(uSideLength))
		{
			throw std::invalid_argument("Block side length must be a power of two.");
		}

		//Compute the side length		
		m_uSideLength = uSideLength;
		m_uSideLengthPower = logBase2(uSideLength);

		Block<VoxelType>::fill(VoxelType());
	}

	template <typename VoxelType>
	uint32_t Block<VoxelType>::calculateSizeInBytes(void)
	{
		uint32_t uSizeInBytes = sizeof(Block<VoxelType>);
		uSizeInBytes += m_vecCompressedData.capacity() * sizeof(RunlengthEntry<uint16_t>);
		return  uSizeInBytes;
	}

	template <typename VoxelType>
	void Block<VoxelType>::compress(void)
	{
		assert(m_bIsCompressed == false);
		assert(m_tUncompressedData != 0);

		//If the uncompressed data hasn't actually been
		//modified then we don't need to redo the compression.
		if(m_bIsUncompressedDataModified)
		{
			uint32_t uNoOfVoxels = m_uSideLength * m_uSideLength * m_uSideLength;
			m_vecCompressedData.clear();

			RunlengthEntry<uint16_t> entry;
			entry.length = 1;
			entry.value = m_tUncompressedData[0];

			for(uint32_t ct = 1; ct < uNoOfVoxels; ++ct)
			{		
				VoxelType value = m_tUncompressedData[ct];
				if((value == entry.value) && (entry.length < entry.maxRunlength()))
				{
					entry.length++;
				}
				else
				{
					m_vecCompressedData.push_back(entry);
					entry.value = value;
					entry.length = 1;
				}
			}

			m_vecCompressedData.push_back(entry);

			//Shrink the vectors to their contents (maybe slow?):
			//http://stackoverflow.com/questions/1111078/reduce-the-capacity-of-an-stl-vector
			//C++0x may have a shrink_to_fit() function?
			std::vector< RunlengthEntry<uint16_t> >(m_vecCompressedData).swap(m_vecCompressedData);
		}

		//Flag the uncompressed data as no longer being used.
		delete[] m_tUncompressedData;
		m_tUncompressedData = 0;
		m_bIsCompressed = true;
	}

	template <typename VoxelType>
	void Block<VoxelType>::uncompress(void)
	{
		assert(m_bIsCompressed == true);
		assert(m_tUncompressedData == 0);
		m_tUncompressedData = new VoxelType[m_uSideLength * m_uSideLength * m_uSideLength];

		VoxelType* pUncompressedData = m_tUncompressedData;		
		for(uint32_t ct = 0; ct < m_vecCompressedData.size(); ++ct)
		{
			std::fill(pUncompressedData, pUncompressedData + m_vecCompressedData[ct].length, m_vecCompressedData[ct].value);
			pUncompressedData += m_vecCompressedData[ct].length;
		}

		m_bIsCompressed = false;
		m_bIsUncompressedDataModified = false;
	}
}
