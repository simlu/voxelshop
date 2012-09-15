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

#ifndef __PolyVox_RaycastWithCallback_H__
#define __PolyVox_RaycastWithCallback_H__

#include "PolyVoxCore/Vector.h"

namespace PolyVox
{
	template<typename VolumeType>
	class RaycastWithCallback
	{
	public:
		///Constructor
		RaycastWithCallback(VolumeType* volData, const Vector3DFloat& v3dStart, const Vector3DFloat& v3dDirectionAndLength, polyvox_function<bool(const Vector3DInt32& position)> funcCallback);

		///Sets the start position for the ray.
		void setStart(const Vector3DFloat& v3dStart);
		///Set the direction for the ray.
		void setDirection(const Vector3DFloat& v3dDirectionAndLength);

		///Performs the raycast.
		void execute();

	private:
		polyvox_function<bool(const Vector3DInt32& position)> m_funcCallback;

		void doRaycast(float x1, float y1, float z1, float x2, float y2, float z2);

		VolumeType* m_volData;
		typename VolumeType::Sampler m_sampVolume;

		Vector3DFloat m_v3dStart;
		Vector3DFloat m_v3dDirectionAndLength;
		float m_fMaxDistance;
	};
}

#include "PolyVoxCore/RaycastWithCallback.inl"

#endif //__PolyVox_RaycastWithCallback_H__