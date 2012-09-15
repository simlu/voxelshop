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
	RawVolume<VoxelType>::RawVolume
	(
		const Region& regValid
	)
	:BaseVolume<VoxelType>(regValid)
	{
		setBorderValue(VoxelType());

		//Create a volume of the right size.
		initialise(regValid);
	}

	////////////////////////////////////////////////////////////////////////////////
	/// Destroys the volume
	////////////////////////////////////////////////////////////////////////////////
	template <typename VoxelType>
	RawVolume<VoxelType>::~RawVolume()
	{
		delete[] m_pData;
		m_pData = 0;
	}

	////////////////////////////////////////////////////////////////////////////////
	/// The border value is returned whenever an attempt is made to read a voxel which
	/// is outside the extents of the volume.
	/// \return The value used for voxels outside of the volume
	////////////////////////////////////////////////////////////////////////////////
	template <typename VoxelType>
	VoxelType RawVolume<VoxelType>::getBorderValue(void) const
	{
		return m_tBorderValue;
	}

	////////////////////////////////////////////////////////////////////////////////
	/// \param uXPos The \c x position of the voxel
	/// \param uYPos The \c y position of the voxel
	/// \param uZPos The \c z position of the voxel
	/// \return The voxel value
	////////////////////////////////////////////////////////////////////////////////
	template <typename VoxelType>
	VoxelType RawVolume<VoxelType>::getVoxelAt(int32_t uXPos, int32_t uYPos, int32_t uZPos) const
	{
		if(this->m_regValidRegion.containsPoint(Vector3DInt32(uXPos, uYPos, uZPos)))
		{
			const Vector3DInt32& v3dLowerCorner = this->m_regValidRegion.getLowerCorner();
			int32_t iLocalXPos = uXPos - v3dLowerCorner.getX();
			int32_t iLocalYPos = uYPos - v3dLowerCorner.getY();
			int32_t iLocalZPos = uZPos - v3dLowerCorner.getZ();

			return m_pData
			[
				iLocalXPos + 
				iLocalYPos * this->getWidth() + 
				iLocalZPos * this->getWidth() * this->getHeight()
			];
		}
		else
		{
			return this->getBorderValue();
		}
	}

	////////////////////////////////////////////////////////////////////////////////
	/// \param v3dPos The 3D position of the voxel
	/// \return The voxel value
	////////////////////////////////////////////////////////////////////////////////
	template <typename VoxelType>
	VoxelType RawVolume<VoxelType>::getVoxelAt(const Vector3DInt32& v3dPos) const
	{
		return getVoxelAt(v3dPos.getX(), v3dPos.getY(), v3dPos.getZ());
	}

	////////////////////////////////////////////////////////////////////////////////
	/// \param tBorder The value to use for voxels outside the volume.
	////////////////////////////////////////////////////////////////////////////////
	template <typename VoxelType>
	void RawVolume<VoxelType>::setBorderValue(const VoxelType& tBorder) 
	{
		m_tBorderValue = tBorder;
	}

	////////////////////////////////////////////////////////////////////////////////
	/// \param uXPos the \c x position of the voxel
	/// \param uYPos the \c y position of the voxel
	/// \param uZPos the \c z position of the voxel
	/// \param tValue the value to which the voxel will be set
	/// \return whether the requested position is inside the volume
	////////////////////////////////////////////////////////////////////////////////
	template <typename VoxelType>
	bool RawVolume<VoxelType>::setVoxelAt(int32_t uXPos, int32_t uYPos, int32_t uZPos, VoxelType tValue)
	{
		if(this->m_regValidRegion.containsPoint(Vector3DInt32(uXPos, uYPos, uZPos)))
		{
			const Vector3DInt32& v3dLowerCorner = this->m_regValidRegion.getLowerCorner();
			int32_t iLocalXPos = uXPos - v3dLowerCorner.getX();
			int32_t iLocalYPos = uYPos - v3dLowerCorner.getY();
			int32_t iLocalZPos = uZPos - v3dLowerCorner.getZ();

			m_pData
			[
				iLocalXPos + 
				iLocalYPos * this->getWidth() + 
				iLocalZPos * this->getWidth() * this->getHeight()
			] = tValue;

			//Return true to indicate that we modified a voxel.
			return true;
		}
		else
		{
			return false;
		}
	}

	////////////////////////////////////////////////////////////////////////////////
	/// \param v3dPos the 3D position of the voxel
	/// \param tValue the value to which the voxel will be set
	/// \return whether the requested position is inside the volume
	////////////////////////////////////////////////////////////////////////////////
	template <typename VoxelType>
	bool RawVolume<VoxelType>::setVoxelAt(const Vector3DInt32& v3dPos, VoxelType tValue)
	{
		return setVoxelAt(v3dPos.getX(), v3dPos.getY(), v3dPos.getZ(), tValue);
	}

	////////////////////////////////////////////////////////////////////////////////
	/// This function should probably be made internal...
	////////////////////////////////////////////////////////////////////////////////
	template <typename VoxelType>
	void RawVolume<VoxelType>::initialise(const Region& regValidRegion)
	{
		this->m_regValidRegion = regValidRegion;

		//Ensure dimensions of the specified Region are valid
		assert(this->getWidth() > 0);
		assert(this->getHeight() > 0);
		assert(this->getDepth() > 0);

		//Create the data
		m_pData = new VoxelType[this->getWidth() * this->getHeight()* this->getDepth()];

		//Other properties we might find useful later
		this->m_uLongestSideLength = (std::max)((std::max)(this->getWidth(),this->getHeight()),this->getDepth());
		this->m_uShortestSideLength = (std::min)((std::min)(this->getWidth(),this->getHeight()),this->getDepth());
		this->m_fDiagonalLength = sqrtf(static_cast<float>(this->getWidth() * this->getWidth() + this->getHeight() * this->getHeight() + this->getDepth() * this->getDepth()));
	}

	////////////////////////////////////////////////////////////////////////////////
	/// Note: This function needs reviewing for accuracy...
	////////////////////////////////////////////////////////////////////////////////
	template <typename VoxelType>
	uint32_t RawVolume<VoxelType>::calculateSizeInBytes(void)
	{
		return this->getWidth() * this->getHeight() * this->getDepth() * sizeof(VoxelType);
	}

}

