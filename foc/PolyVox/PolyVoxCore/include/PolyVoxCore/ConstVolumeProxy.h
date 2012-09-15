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

#ifndef __PolyVox_ConstVolumeProxy_H__
#define __PolyVox_ConstVolumeProxy_H__

#include "PolyVoxCore/Region.h"
#include "PolyVoxCore/Vector.h"

namespace PolyVox
{
	template <typename VoxelType>
	class ConstVolumeProxy
	{
		//LargeVolume is a friend so it can call the constructor.
		friend class LargeVolume<VoxelType>;
	public:
		VoxelType getVoxelAt(int32_t uXPos, int32_t uYPos, int32_t uZPos) const
		{
			assert(m_regValid.containsPoint(Vector3DInt32(uXPos, uYPos, uZPos)));
			return m_pVolume.getVoxelAt(uXPos, uYPos, uZPos);
		}

		VoxelType getVoxelAt(const Vector3DInt32& v3dPos) const
		{
			assert(m_regValid.containsPoint(v3dPos));
			return getVoxelAt(v3dPos.getX(), v3dPos.getY(), v3dPos.getZ());
		}

		void setVoxelAt(int32_t uXPos, int32_t uYPos, int32_t uZPos, VoxelType tValue) const
		{
			assert(m_regValid.containsPoint(Vector3DInt32(uXPos, uYPos, uZPos)));
			m_pVolume.setVoxelAtConst(uXPos, uYPos, uZPos, tValue);
		}

		void setVoxelAt(const Vector3DInt32& v3dPos, VoxelType tValue) const
		{
			assert(m_regValid.containsPoint(v3dPos));
			setVoxelAt(v3dPos.getX(), v3dPos.getY(), v3dPos.getZ(), tValue);
		}
	private:
		//Private constructor, so client code can't abuse this class.
		ConstVolumeProxy(const LargeVolume<VoxelType>& pVolume, const Region& regValid)
			:m_pVolume(pVolume)
			,m_regValid(regValid)
		{
		}

		//Private assignment operator, so client code can't abuse this class.
		ConstVolumeProxy& operator=(const ConstVolumeProxy& rhs) throw()
		{
		}

		const LargeVolume<VoxelType>& m_pVolume;
		const Region& m_regValid;
	};
}

#endif //__PolyVox_ConstVolumeProxy_H__
