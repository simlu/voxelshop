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

#include "PolyVoxCore/VoxelFilters.h"

namespace PolyVox
{
	template< typename VolumeType >
	float computeSmoothedVoxel(typename VolumeType::Sampler& volIter)
	{
		float sum = 0.0;

		if(volIter.peekVoxel1nx1ny1nz() != 0) sum += 1.0f;
		if(volIter.peekVoxel1nx1ny0pz() != 0) sum += 1.0f;
		if(volIter.peekVoxel1nx1ny1pz() != 0) sum += 1.0f;
		if(volIter.peekVoxel1nx0py1nz() != 0) sum += 1.0f;
		if(volIter.peekVoxel1nx0py0pz() != 0) sum += 1.0f;
		if(volIter.peekVoxel1nx0py1pz() != 0) sum += 1.0f;
		if(volIter.peekVoxel1nx1py1nz() != 0) sum += 1.0f;
		if(volIter.peekVoxel1nx1py0pz() != 0) sum += 1.0f;
		if(volIter.peekVoxel1nx1py1pz() != 0) sum += 1.0f;

		if(volIter.peekVoxel0px1ny1nz() != 0) sum += 1.0f;
		if(volIter.peekVoxel0px1ny0pz() != 0) sum += 1.0f;
		if(volIter.peekVoxel0px1ny1pz() != 0) sum += 1.0f;
		if(volIter.peekVoxel0px0py1nz() != 0) sum += 1.0f;
		if(volIter.getVoxel() != 0) sum += 1.0f;
		if(volIter.peekVoxel0px0py1pz() != 0) sum += 1.0f;
		if(volIter.peekVoxel0px1py1nz() != 0) sum += 1.0f;
		if(volIter.peekVoxel0px1py0pz() != 0) sum += 1.0f;
		if(volIter.peekVoxel0px1py1pz() != 0) sum += 1.0f;

		if(volIter.peekVoxel1px1ny1nz() != 0) sum += 1.0f;
		if(volIter.peekVoxel1px1ny0pz() != 0) sum += 1.0f;
		if(volIter.peekVoxel1px1ny1pz() != 0) sum += 1.0f;
		if(volIter.peekVoxel1px0py1nz() != 0) sum += 1.0f;
		if(volIter.peekVoxel1px0py0pz() != 0) sum += 1.0f;
		if(volIter.peekVoxel1px0py1pz() != 0) sum += 1.0f;
		if(volIter.peekVoxel1px1py1nz() != 0) sum += 1.0f;
		if(volIter.peekVoxel1px1py0pz() != 0) sum += 1.0f;
		if(volIter.peekVoxel1px1py1pz() != 0) sum += 1.0f;

		sum /= 27.0f;
		return sum;
	}
}
