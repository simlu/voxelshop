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
	////////////////////////////////////////////////////////////////////////////////
	/// This constructor creates a volume with a fixed size which is specified as a parameter.
	/// \param regValid Specifies the minimum and maximum valid voxel positions.
	////////////////////////////////////////////////////////////////////////////////
	template <typename VoxelType>
	SimpleVolume<VoxelType>::SimpleVolume
	(
		const Region& regValid,
		uint16_t uBlockSideLength
	)
	:BaseVolume<VoxelType>(regValid)
	{
		//Create a volume of the right size.
		initialise(regValid,uBlockSideLength);
	}

	////////////////////////////////////////////////////////////////////////////////
	/// Destroys the volume
	////////////////////////////////////////////////////////////////////////////////
	template <typename VoxelType>
	SimpleVolume<VoxelType>::~SimpleVolume()
	{
		delete[] m_pBlocks;
		delete[] m_pUncompressedBorderData;
	}

	////////////////////////////////////////////////////////////////////////////////
	/// The border value is returned whenever an atempt is made to read a voxel which
	/// is outside the extents of the volume.
	/// \return The value used for voxels outside of the volume
	////////////////////////////////////////////////////////////////////////////////
	template <typename VoxelType>
	VoxelType SimpleVolume<VoxelType>::getBorderValue(void) const
	{
		return *m_pUncompressedBorderData;
	}

	////////////////////////////////////////////////////////////////////////////////
	/// \param uXPos The \c x position of the voxel
	/// \param uYPos The \c y position of the voxel
	/// \param uZPos The \c z position of the voxel
	/// \return The voxel value
	////////////////////////////////////////////////////////////////////////////////
	template <typename VoxelType>
	VoxelType SimpleVolume<VoxelType>::getVoxelAt(int32_t uXPos, int32_t uYPos, int32_t uZPos) const
	{
		if(this->m_regValidRegion.containsPoint(Vector3DInt32(uXPos, uYPos, uZPos)))
		{
			const int32_t blockX = uXPos >> m_uBlockSideLengthPower;
			const int32_t blockY = uYPos >> m_uBlockSideLengthPower;
			const int32_t blockZ = uZPos >> m_uBlockSideLengthPower;

			const uint16_t xOffset = static_cast<uint16_t>(uXPos - (blockX << m_uBlockSideLengthPower));
			const uint16_t yOffset = static_cast<uint16_t>(uYPos - (blockY << m_uBlockSideLengthPower));
			const uint16_t zOffset = static_cast<uint16_t>(uZPos - (blockZ << m_uBlockSideLengthPower));

			typename SimpleVolume<VoxelType>::Block* pUncompressedBlock = getUncompressedBlock(blockX, blockY, blockZ);

			return pUncompressedBlock->getVoxelAt(xOffset,yOffset,zOffset);
		}
		else
		{
			return getBorderValue();
		}
	}

	////////////////////////////////////////////////////////////////////////////////
	/// \param v3dPos The 3D position of the voxel
	/// \return The voxel value
	////////////////////////////////////////////////////////////////////////////////
	template <typename VoxelType>
	VoxelType SimpleVolume<VoxelType>::getVoxelAt(const Vector3DInt32& v3dPos) const
	{
		return getVoxelAt(v3dPos.getX(), v3dPos.getY(), v3dPos.getZ());
	}

	////////////////////////////////////////////////////////////////////////////////
	/// \param tBorder The value to use for voxels outside the volume.
	////////////////////////////////////////////////////////////////////////////////
	template <typename VoxelType>
	void SimpleVolume<VoxelType>::setBorderValue(const VoxelType& tBorder) 
	{
		/*Block<VoxelType>* pUncompressedBorderBlock = getUncompressedBlock(&m_pBorderBlock);
		return pUncompressedBorderBlock->fill(tBorder);*/
		std::fill(m_pUncompressedBorderData, m_pUncompressedBorderData + m_uNoOfVoxelsPerBlock, tBorder);
	}

	////////////////////////////////////////////////////////////////////////////////
	/// \param uXPos the \c x position of the voxel
	/// \param uYPos the \c y position of the voxel
	/// \param uZPos the \c z position of the voxel
	/// \param tValue the value to which the voxel will be set
	/// \return whether the requested position is inside the volume
	////////////////////////////////////////////////////////////////////////////////
	template <typename VoxelType>
	bool SimpleVolume<VoxelType>::setVoxelAt(int32_t uXPos, int32_t uYPos, int32_t uZPos, VoxelType tValue)
	{
		assert(this->m_regValidRegion.containsPoint(Vector3DInt32(uXPos, uYPos, uZPos)));

		const int32_t blockX = uXPos >> m_uBlockSideLengthPower;
		const int32_t blockY = uYPos >> m_uBlockSideLengthPower;
		const int32_t blockZ = uZPos >> m_uBlockSideLengthPower;

		const uint16_t xOffset = uXPos - (blockX << m_uBlockSideLengthPower);
		const uint16_t yOffset = uYPos - (blockY << m_uBlockSideLengthPower);
		const uint16_t zOffset = uZPos - (blockZ << m_uBlockSideLengthPower);

		typename SimpleVolume<VoxelType>::Block* pUncompressedBlock = getUncompressedBlock(blockX, blockY, blockZ);

		pUncompressedBlock->setVoxelAt(xOffset,yOffset,zOffset, tValue);

		//Return true to indicate that we modified a voxel.
		return true;
	}

	////////////////////////////////////////////////////////////////////////////////
	/// \param v3dPos the 3D position of the voxel
	/// \param tValue the value to which the voxel will be set
	/// \return whether the requested position is inside the volume
	////////////////////////////////////////////////////////////////////////////////
	template <typename VoxelType>
	bool SimpleVolume<VoxelType>::setVoxelAt(const Vector3DInt32& v3dPos, VoxelType tValue)
	{
		return setVoxelAt(v3dPos.getX(), v3dPos.getY(), v3dPos.getZ(), tValue);
	}

	////////////////////////////////////////////////////////////////////////////////
	/// This function should probably be made internal...
	////////////////////////////////////////////////////////////////////////////////
	template <typename VoxelType>
	void SimpleVolume<VoxelType>::initialise(const Region& regValidRegion, uint16_t uBlockSideLength)
	{
		//Debug mode validation
		assert(uBlockSideLength >= 8);
		assert(uBlockSideLength <= 256);
		assert(isPowerOf2(uBlockSideLength));
		
		//Release mode validation
		if(uBlockSideLength < 8)
		{
			throw std::invalid_argument("Block side length should be at least 8");
		}
		if(uBlockSideLength > 256)
		{
			throw std::invalid_argument("Block side length should not be more than 256");
		}
		if(!isPowerOf2(uBlockSideLength))
		{
			throw std::invalid_argument("Block side length must be a power of two.");
		}

		m_uBlockSideLength = uBlockSideLength;
		m_uNoOfVoxelsPerBlock = m_uBlockSideLength * m_uBlockSideLength * m_uBlockSideLength;
		m_pUncompressedBorderData = 0;

		this->m_regValidRegion = regValidRegion;

		m_regValidRegionInBlocks.setLowerCorner(this->m_regValidRegion.getLowerCorner()  / static_cast<int32_t>(uBlockSideLength));
		m_regValidRegionInBlocks.setUpperCorner(this->m_regValidRegion.getUpperCorner()  / static_cast<int32_t>(uBlockSideLength));

		//Compute the block side length
		m_uBlockSideLength = uBlockSideLength;
		m_uBlockSideLengthPower = logBase2(m_uBlockSideLength);

		//Compute the size of the volume in blocks (and note +1 at the end)
		m_uWidthInBlocks = m_regValidRegionInBlocks.getUpperCorner().getX() - m_regValidRegionInBlocks.getLowerCorner().getX() + 1;
		m_uHeightInBlocks = m_regValidRegionInBlocks.getUpperCorner().getY() - m_regValidRegionInBlocks.getLowerCorner().getY() + 1;
		m_uDepthInBlocks = m_regValidRegionInBlocks.getUpperCorner().getZ() - m_regValidRegionInBlocks.getLowerCorner().getZ() + 1;
		m_uNoOfBlocksInVolume = m_uWidthInBlocks * m_uHeightInBlocks * m_uDepthInBlocks;

		//Allocate the data
		m_pBlocks = new Block[m_uNoOfBlocksInVolume];
		for(uint32_t i = 0; i < m_uNoOfBlocksInVolume; ++i)
		{
			m_pBlocks[i].initialise(m_uBlockSideLength);
		}

		//Create the border block
		m_pUncompressedBorderData = new VoxelType[m_uNoOfVoxelsPerBlock];
		std::fill(m_pUncompressedBorderData, m_pUncompressedBorderData + m_uNoOfVoxelsPerBlock, VoxelType());

		//Other properties we might find useful later
		this->m_uLongestSideLength = (std::max)((std::max)(this->getWidth(),this->getHeight()),this->getDepth());
		this->m_uShortestSideLength = (std::min)((std::min)(this->getWidth(),this->getHeight()),this->getDepth());
		this->m_fDiagonalLength = sqrtf(static_cast<float>(this->getWidth() * this->getWidth() + this->getHeight() * this->getHeight() + this->getDepth() * this->getDepth()));
	}

	template <typename VoxelType>
	typename SimpleVolume<VoxelType>::Block* SimpleVolume<VoxelType>::getUncompressedBlock(int32_t uBlockX, int32_t uBlockY, int32_t uBlockZ) const
	{
		//The lower left corner of the volume could be
		//anywhere, but array indices need to start at zero.
		uBlockX -= m_regValidRegionInBlocks.getLowerCorner().getX();
		uBlockY -= m_regValidRegionInBlocks.getLowerCorner().getY();
		uBlockZ -= m_regValidRegionInBlocks.getLowerCorner().getZ();

		//Compute the block index
		uint32_t uBlockIndex =
				uBlockX + 
				uBlockY * m_uWidthInBlocks + 
				uBlockZ * m_uWidthInBlocks * m_uHeightInBlocks;

		//Return the block
		return &(m_pBlocks[uBlockIndex]);
	}

	////////////////////////////////////////////////////////////////////////////////
	/// Note: This function needs reviewing for accuracy...
	////////////////////////////////////////////////////////////////////////////////
	template <typename VoxelType>
	uint32_t SimpleVolume<VoxelType>::calculateSizeInBytes(void)
	{
		uint32_t uSizeInBytes = sizeof(SimpleVolume);
		
		uint32_t uSizeOfBlockInBytes = m_uNoOfVoxelsPerBlock * sizeof(VoxelType);

		//Memory used by the blocks ( + 1 is for border)
		uSizeInBytes += uSizeOfBlockInBytes * (m_uNoOfBlocksInVolume + 1);

		return uSizeInBytes;
	}

}

