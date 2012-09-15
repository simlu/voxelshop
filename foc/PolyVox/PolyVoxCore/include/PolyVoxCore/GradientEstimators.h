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

#ifndef __PolyVox_GradientEstimators_H__
#define __PolyVox_GradientEstimators_H__

#include "PolyVoxCore/Vector.h"
#include "PolyVoxCore/VoxelFilters.h"

#include <vector>

namespace PolyVox
{
	enum NormalGenerationMethod
	{
		SIMPLE, ///<Fastest
		CENTRAL_DIFFERENCE,
		SOBEL,
		CENTRAL_DIFFERENCE_SMOOTHED,
		SOBEL_SMOOTHED ///<Smoothest
	};

	template<typename VolumeType>
	Vector3DFloat computeCentralDifferenceGradient(const typename VolumeType::Sampler& volIter);

	template<typename VolumeType>
	Vector3DFloat computeSmoothCentralDifferenceGradient(typename VolumeType::Sampler& volIter);

	template<typename VolumeType>
	Vector3DFloat computeDecimatedCentralDifferenceGradient(typename VolumeType::Sampler& volIter);

	template<typename VolumeType>
	Vector3DFloat computeSobelGradient(const typename VolumeType::Sampler& volIter);

	template<typename VolumeType>
	Vector3DFloat computeSmoothSobelGradient(typename VolumeType::Sampler& volIter);

	//POLYVOX_API void computeNormalsForVertices(VolumeType<uint8_t>* volumeData, SurfaceMesh<PositionMaterialNormal>& mesh, NormalGenerationMethod normalGenerationMethod);
	//POLYVOX_API Vector3DFloat computeNormal(VolumeType<uint8_t>* volumeData, const Vector3DFloat& v3dPos, NormalGenerationMethod normalGenerationMethod);
}

#include "PolyVoxCore/GradientEstimators.inl"

#endif //__PolyVox_GradientEstimators_H__
