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
	template <typename DerivedVolumeType>
	BaseVolume<VoxelType>::Sampler<DerivedVolumeType>::Sampler(DerivedVolumeType* volume)
		:mVolume(volume)
		,mXPosInVolume(0)
		,mYPosInVolume(0)
		,mZPosInVolume(0)
	{
	}

	template <typename VoxelType>
	template <typename DerivedVolumeType>
	BaseVolume<VoxelType>::Sampler<DerivedVolumeType>::~Sampler()
	{
	}

	template <typename VoxelType>
	template <typename DerivedVolumeType>
	Vector3DInt32 BaseVolume<VoxelType>::Sampler<DerivedVolumeType>::getPosition(void) const
	{
		return Vector3DInt32(mXPosInVolume, mYPosInVolume, mZPosInVolume);
	}

	template <typename VoxelType>
	template <typename DerivedVolumeType>
	VoxelType BaseVolume<VoxelType>::Sampler<DerivedVolumeType>::getVoxel(void) const
	{
		return mVolume->getVoxelAt(mXPosInVolume, mYPosInVolume, mZPosInVolume);
	}

	template <typename VoxelType>
	template <typename DerivedVolumeType>
	void BaseVolume<VoxelType>::Sampler<DerivedVolumeType>::setPosition(const Vector3DInt32& v3dNewPos)
	{
		mXPosInVolume = v3dNewPos.getX();
		mYPosInVolume = v3dNewPos.getY();
		mZPosInVolume = v3dNewPos.getZ();
	}

	template <typename VoxelType>
	template <typename DerivedVolumeType>
	void BaseVolume<VoxelType>::Sampler<DerivedVolumeType>::setPosition(int32_t xPos, int32_t yPos, int32_t zPos)
	{
		mXPosInVolume = xPos;
		mYPosInVolume = yPos;
		mZPosInVolume = zPos;
	}

	template <typename VoxelType>
	template <typename DerivedVolumeType>
	bool BaseVolume<VoxelType>::Sampler<DerivedVolumeType>::setVoxel(VoxelType tValue)
	{
		return mVolume->setVoxelAt(mXPosInVolume, mYPosInVolume, mZPosInVolume, tValue);
	}

	template <typename VoxelType>
	template <typename DerivedVolumeType>
	void BaseVolume<VoxelType>::Sampler<DerivedVolumeType>::movePositiveX(void)
	{
		mXPosInVolume++;
	}

	template <typename VoxelType>
	template <typename DerivedVolumeType>
	void BaseVolume<VoxelType>::Sampler<DerivedVolumeType>::movePositiveY(void)
	{
		mYPosInVolume++;
	}

	template <typename VoxelType>
	template <typename DerivedVolumeType>
	void BaseVolume<VoxelType>::Sampler<DerivedVolumeType>::movePositiveZ(void)
	{
		mZPosInVolume++;
	}

	template <typename VoxelType>
	template <typename DerivedVolumeType>
	void BaseVolume<VoxelType>::Sampler<DerivedVolumeType>::moveNegativeX(void)
	{
		mXPosInVolume--;
	}

	template <typename VoxelType>
	template <typename DerivedVolumeType>
	void BaseVolume<VoxelType>::Sampler<DerivedVolumeType>::moveNegativeY(void)
	{
		mYPosInVolume--;
	}

	template <typename VoxelType>
	template <typename DerivedVolumeType>
	void BaseVolume<VoxelType>::Sampler<DerivedVolumeType>::moveNegativeZ(void)
	{
		mZPosInVolume--;
	}

	template <typename VoxelType>
	template <typename DerivedVolumeType>
	VoxelType BaseVolume<VoxelType>::Sampler<DerivedVolumeType>::peekVoxel1nx1ny1nz(void) const
	{
		return mVolume->getVoxelAt(mXPosInVolume - 1, mYPosInVolume - 1, mZPosInVolume - 1);
	}

	template <typename VoxelType>
	template <typename DerivedVolumeType>
	VoxelType BaseVolume<VoxelType>::Sampler<DerivedVolumeType>::peekVoxel1nx1ny0pz(void) const
	{
		return mVolume->getVoxelAt(mXPosInVolume - 1, mYPosInVolume - 1, mZPosInVolume    );
	}

	template <typename VoxelType>
	template <typename DerivedVolumeType>
	VoxelType BaseVolume<VoxelType>::Sampler<DerivedVolumeType>::peekVoxel1nx1ny1pz(void) const
	{
		return mVolume->getVoxelAt(mXPosInVolume - 1, mYPosInVolume - 1, mZPosInVolume + 1);
	}

	template <typename VoxelType>
	template <typename DerivedVolumeType>
	VoxelType BaseVolume<VoxelType>::Sampler<DerivedVolumeType>::peekVoxel1nx0py1nz(void) const
	{
		return mVolume->getVoxelAt(mXPosInVolume - 1, mYPosInVolume    , mZPosInVolume - 1);
	}

	template <typename VoxelType>
	template <typename DerivedVolumeType>
	VoxelType BaseVolume<VoxelType>::Sampler<DerivedVolumeType>::peekVoxel1nx0py0pz(void) const
	{
		return mVolume->getVoxelAt(mXPosInVolume - 1, mYPosInVolume    , mZPosInVolume    );
	}

	template <typename VoxelType>
	template <typename DerivedVolumeType>
	VoxelType BaseVolume<VoxelType>::Sampler<DerivedVolumeType>::peekVoxel1nx0py1pz(void) const
	{
		return mVolume->getVoxelAt(mXPosInVolume - 1, mYPosInVolume    , mZPosInVolume + 1);
	}

	template <typename VoxelType>
	template <typename DerivedVolumeType>
	VoxelType BaseVolume<VoxelType>::Sampler<DerivedVolumeType>::peekVoxel1nx1py1nz(void) const
	{
		return mVolume->getVoxelAt(mXPosInVolume - 1, mYPosInVolume + 1, mZPosInVolume - 1);
	}

	template <typename VoxelType>
	template <typename DerivedVolumeType>
	VoxelType BaseVolume<VoxelType>::Sampler<DerivedVolumeType>::peekVoxel1nx1py0pz(void) const
	{
		return mVolume->getVoxelAt(mXPosInVolume - 1, mYPosInVolume + 1, mZPosInVolume    );
	}

	template <typename VoxelType>
	template <typename DerivedVolumeType>
	VoxelType BaseVolume<VoxelType>::Sampler<DerivedVolumeType>::peekVoxel1nx1py1pz(void) const
	{
		return mVolume->getVoxelAt(mXPosInVolume - 1, mYPosInVolume + 1, mZPosInVolume + 1);
	}

	//////////////////////////////////////////////////////////////////////////

	template <typename VoxelType>
	template <typename DerivedVolumeType>
	VoxelType BaseVolume<VoxelType>::Sampler<DerivedVolumeType>::peekVoxel0px1ny1nz(void) const
	{
		return mVolume->getVoxelAt(mXPosInVolume    , mYPosInVolume - 1, mZPosInVolume - 1);
	}

	template <typename VoxelType>
	template <typename DerivedVolumeType>
	VoxelType BaseVolume<VoxelType>::Sampler<DerivedVolumeType>::peekVoxel0px1ny0pz(void) const
	{
		return mVolume->getVoxelAt(mXPosInVolume    , mYPosInVolume - 1, mZPosInVolume    );
	}

	template <typename VoxelType>
	template <typename DerivedVolumeType>
	VoxelType BaseVolume<VoxelType>::Sampler<DerivedVolumeType>::peekVoxel0px1ny1pz(void) const
	{
		return mVolume->getVoxelAt(mXPosInVolume    , mYPosInVolume - 1, mZPosInVolume + 1);
	}

	template <typename VoxelType>
	template <typename DerivedVolumeType>
	VoxelType BaseVolume<VoxelType>::Sampler<DerivedVolumeType>::peekVoxel0px0py1nz(void) const
	{
		return mVolume->getVoxelAt(mXPosInVolume    , mYPosInVolume    , mZPosInVolume - 1);
	}

	template <typename VoxelType>
	template <typename DerivedVolumeType>
	VoxelType BaseVolume<VoxelType>::Sampler<DerivedVolumeType>::peekVoxel0px0py0pz(void) const
	{
		return mVolume->getVoxelAt(mXPosInVolume    , mYPosInVolume    , mZPosInVolume    );
	}

	template <typename VoxelType>
	template <typename DerivedVolumeType>
	VoxelType BaseVolume<VoxelType>::Sampler<DerivedVolumeType>::peekVoxel0px0py1pz(void) const
	{
		return mVolume->getVoxelAt(mXPosInVolume    , mYPosInVolume    , mZPosInVolume + 1);
	}

	template <typename VoxelType>
	template <typename DerivedVolumeType>
	VoxelType BaseVolume<VoxelType>::Sampler<DerivedVolumeType>::peekVoxel0px1py1nz(void) const
	{
		return mVolume->getVoxelAt(mXPosInVolume    , mYPosInVolume + 1, mZPosInVolume - 1);
	}

	template <typename VoxelType>
	template <typename DerivedVolumeType>
	VoxelType BaseVolume<VoxelType>::Sampler<DerivedVolumeType>::peekVoxel0px1py0pz(void) const
	{
		return mVolume->getVoxelAt(mXPosInVolume    , mYPosInVolume + 1, mZPosInVolume    );
	}

	template <typename VoxelType>
	template <typename DerivedVolumeType>
	VoxelType BaseVolume<VoxelType>::Sampler<DerivedVolumeType>::peekVoxel0px1py1pz(void) const
	{
		return mVolume->getVoxelAt(mXPosInVolume    , mYPosInVolume + 1, mZPosInVolume + 1);
	}

	//////////////////////////////////////////////////////////////////////////

	template <typename VoxelType>
	template <typename DerivedVolumeType>
	VoxelType BaseVolume<VoxelType>::Sampler<DerivedVolumeType>::peekVoxel1px1ny1nz(void) const
	{
		return mVolume->getVoxelAt(mXPosInVolume + 1, mYPosInVolume - 1, mZPosInVolume - 1);
	}

	template <typename VoxelType>
	template <typename DerivedVolumeType>
	VoxelType BaseVolume<VoxelType>::Sampler<DerivedVolumeType>::peekVoxel1px1ny0pz(void) const
	{
		return mVolume->getVoxelAt(mXPosInVolume + 1, mYPosInVolume - 1, mZPosInVolume    );
	}

	template <typename VoxelType>
	template <typename DerivedVolumeType>
	VoxelType BaseVolume<VoxelType>::Sampler<DerivedVolumeType>::peekVoxel1px1ny1pz(void) const
	{
		return mVolume->getVoxelAt(mXPosInVolume + 1, mYPosInVolume - 1, mZPosInVolume + 1);
	}

	template <typename VoxelType>
	template <typename DerivedVolumeType>
	VoxelType BaseVolume<VoxelType>::Sampler<DerivedVolumeType>::peekVoxel1px0py1nz(void) const
	{
		return mVolume->getVoxelAt(mXPosInVolume + 1, mYPosInVolume    , mZPosInVolume - 1);
	}

	template <typename VoxelType>
	template <typename DerivedVolumeType>
	VoxelType BaseVolume<VoxelType>::Sampler<DerivedVolumeType>::peekVoxel1px0py0pz(void) const
	{
		return mVolume->getVoxelAt(mXPosInVolume + 1, mYPosInVolume    , mZPosInVolume    );
	}

	template <typename VoxelType>
	template <typename DerivedVolumeType>
	VoxelType BaseVolume<VoxelType>::Sampler<DerivedVolumeType>::peekVoxel1px0py1pz(void) const
	{
		return mVolume->getVoxelAt(mXPosInVolume + 1, mYPosInVolume    , mZPosInVolume + 1);
	}

	template <typename VoxelType>
	template <typename DerivedVolumeType>
	VoxelType BaseVolume<VoxelType>::Sampler<DerivedVolumeType>::peekVoxel1px1py1nz(void) const
	{
		return mVolume->getVoxelAt(mXPosInVolume + 1, mYPosInVolume + 1, mZPosInVolume - 1);
	}

	template <typename VoxelType>
	template <typename DerivedVolumeType>
	VoxelType BaseVolume<VoxelType>::Sampler<DerivedVolumeType>::peekVoxel1px1py0pz(void) const
	{
		return mVolume->getVoxelAt(mXPosInVolume + 1, mYPosInVolume + 1, mZPosInVolume    );
	}

	template <typename VoxelType>
	template <typename DerivedVolumeType>
	VoxelType BaseVolume<VoxelType>::Sampler<DerivedVolumeType>::peekVoxel1px1py1pz(void) const
	{
		return mVolume->getVoxelAt(mXPosInVolume + 1, mYPosInVolume + 1, mZPosInVolume + 1);
	}
}
