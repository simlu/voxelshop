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
	BaseVolume<VoxelType>::BaseVolume
	(
		const Region& regValid
	)
		:m_regValidRegion(regValid)
	{
	}

	////////////////////////////////////////////////////////////////////////////////
	/// Destroys the volume
	////////////////////////////////////////////////////////////////////////////////
	template <typename VoxelType>
	BaseVolume<VoxelType>::~BaseVolume()
	{
	}

	////////////////////////////////////////////////////////////////////////////////
	/// The border value is returned whenever an attempt is made to read a voxel which
	/// is outside the extents of the volume.
	/// \return The value used for voxels outside of the volume
	////////////////////////////////////////////////////////////////////////////////
	template <typename VoxelType>
	VoxelType BaseVolume<VoxelType>::getBorderValue(void) const
	{
		assert(false);
		return VoxelType();
	}

	////////////////////////////////////////////////////////////////////////////////
	/// \return A Region representing the extent of the volume.
	////////////////////////////////////////////////////////////////////////////////
	template <typename VoxelType>
	Region BaseVolume<VoxelType>::getEnclosingRegion(void) const
	{
		return m_regValidRegion;
	}

	////////////////////////////////////////////////////////////////////////////////
	/// \return The width of the volume in voxels. Note that this value is inclusive, so that if the valid range is e.g. 0 to 63 then the width is 64.
	/// \sa getHeight(), getDepth()
	////////////////////////////////////////////////////////////////////////////////
	template <typename VoxelType>
	int32_t BaseVolume<VoxelType>::getWidth(void) const
	{
		return m_regValidRegion.getUpperCorner().getX() - m_regValidRegion.getLowerCorner().getX() + 1;
	}

	////////////////////////////////////////////////////////////////////////////////
	/// \return The height of the volume in voxels. Note that this value is inclusive, so that if the valid range is e.g. 0 to 63 then the height is 64.
	/// \sa getWidth(), getDepth()
	////////////////////////////////////////////////////////////////////////////////
	template <typename VoxelType>
	int32_t BaseVolume<VoxelType>::getHeight(void) const
	{
		return m_regValidRegion.getUpperCorner().getY() - m_regValidRegion.getLowerCorner().getY() + 1;
	}

	////////////////////////////////////////////////////////////////////////////////
	/// \return The depth of the volume in voxels. Note that this value is inclusive, so that if the valid range is e.g. 0 to 63 then the depth is 64.
	/// \sa getWidth(), getHeight()
	////////////////////////////////////////////////////////////////////////////////
	template <typename VoxelType>
	int32_t BaseVolume<VoxelType>::getDepth(void) const
	{
		return m_regValidRegion.getUpperCorner().getZ() - m_regValidRegion.getLowerCorner().getZ() + 1;
	}

	////////////////////////////////////////////////////////////////////////////////
	/// \return The length of the shortest side in voxels. For example, if a volume has
	/// dimensions 256x512x1024 this function will return 256.
	/// \sa getLongestSideLength(), getDiagonalLength()
	////////////////////////////////////////////////////////////////////////////////
	template <typename VoxelType>
	int32_t BaseVolume<VoxelType>::getShortestSideLength(void) const
	{
		return m_uShortestSideLength;
	}

	////////////////////////////////////////////////////////////////////////////////
	/// \return The length of the longest side in voxels. For example, if a volume has
	/// dimensions 256x512x1024 this function will return 1024.
	/// \sa getShortestSideLength(), getDiagonalLength()
	////////////////////////////////////////////////////////////////////////////////
	template <typename VoxelType>
	int32_t BaseVolume<VoxelType>::getLongestSideLength(void) const
	{
		return m_uLongestSideLength;
	}	

	////////////////////////////////////////////////////////////////////////////////
	/// \return The length of the diagonal in voxels. For example, if a volume has
	/// dimensions 256x512x1024 this function will return sqrt(256*256+512*512+1024*1024)
	/// = 1173.139. This value is computed on volume creation so retrieving it is fast.
	/// \sa getShortestSideLength(), getLongestSideLength()
	////////////////////////////////////////////////////////////////////////////////
	template <typename VoxelType>
	float BaseVolume<VoxelType>::getDiagonalLength(void) const
	{
		return m_fDiagonalLength;
	}

	////////////////////////////////////////////////////////////////////////////////
	/// \param uXPos The \c x position of the voxel
	/// \param uYPos The \c y position of the voxel
	/// \param uZPos The \c z position of the voxel
	/// \return The voxel value
	////////////////////////////////////////////////////////////////////////////////
	template <typename VoxelType>
	VoxelType BaseVolume<VoxelType>::getVoxelAt(int32_t uXPos, int32_t uYPos, int32_t uZPos) const
	{
		assert(false);
		return VoxelType();
	}

	////////////////////////////////////////////////////////////////////////////////
	/// \param v3dPos The 3D position of the voxel
	/// \return The voxel value
	////////////////////////////////////////////////////////////////////////////////
	template <typename VoxelType>
	VoxelType BaseVolume<VoxelType>::getVoxelAt(const Vector3DInt32& v3dPos) const
	{
		assert(false);
		return VoxelType();
	}

	////////////////////////////////////////////////////////////////////////////////
	/// \param tBorder The value to use for voxels outside the volume.
	////////////////////////////////////////////////////////////////////////////////
	template <typename VoxelType>
	void BaseVolume<VoxelType>::setBorderValue(const VoxelType& tBorder) 
	{
		assert(false);
	}

	////////////////////////////////////////////////////////////////////////////////
	/// \param uXPos the \c x position of the voxel
	/// \param uYPos the \c y position of the voxel
	/// \param uZPos the \c z position of the voxel
	/// \param tValue the value to which the voxel will be set
	/// \return whether the requested position is inside the volume
	////////////////////////////////////////////////////////////////////////////////
	template <typename VoxelType>
	bool BaseVolume<VoxelType>::setVoxelAt(int32_t uXPos, int32_t uYPos, int32_t uZPos, VoxelType tValue)
	{
		assert(false);
		return false;
	}

	////////////////////////////////////////////////////////////////////////////////
	/// \param v3dPos the 3D position of the voxel
	/// \param tValue the value to which the voxel will be set
	/// \return whether the requested position is inside the volume
	////////////////////////////////////////////////////////////////////////////////
	template <typename VoxelType>
	bool BaseVolume<VoxelType>::setVoxelAt(const Vector3DInt32& v3dPos, VoxelType tValue)
	{
		assert(false);
		return false;
	}

	////////////////////////////////////////////////////////////////////////////////
	/// Note: This function needs reviewing for accuracy...
	////////////////////////////////////////////////////////////////////////////////
	template <typename VoxelType>
	uint32_t BaseVolume<VoxelType>::calculateSizeInBytes(void)
	{
		return getWidth() * getHeight() * getDepth() * sizeof(VoxelType);
	}	
}

