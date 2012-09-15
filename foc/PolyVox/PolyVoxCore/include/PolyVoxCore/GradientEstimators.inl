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
	template<typename VolumeType>
	Vector3DFloat computeCentralDifferenceGradient(const typename VolumeType::Sampler& volIter)
	{
		//FIXME - bitwise way of doing this?
		typename VolumeType::VoxelType voxel1nx = volIter.peekVoxel1nx0py0pz() > 0 ? 1: 0;
		typename VolumeType::VoxelType voxel1px = volIter.peekVoxel1px0py0pz() > 0 ? 1: 0;

		typename VolumeType::VoxelType voxel1ny = volIter.peekVoxel0px1ny0pz() > 0 ? 1: 0;
		typename VolumeType::VoxelType voxel1py = volIter.peekVoxel0px1py0pz() > 0 ? 1: 0;

		typename VolumeType::VoxelType voxel1nz = volIter.peekVoxel0px0py1nz() > 0 ? 1: 0;
		typename VolumeType::VoxelType voxel1pz = volIter.peekVoxel0px0py1pz() > 0 ? 1: 0;

		return Vector3DFloat
		(
			static_cast<float>(voxel1nx) - static_cast<float>(voxel1px),
			static_cast<float>(voxel1ny) - static_cast<float>(voxel1py),
			static_cast<float>(voxel1nz) - static_cast<float>(voxel1pz)
		);
	}

	template<typename VolumeType>
	Vector3DFloat computeDecimatedCentralDifferenceGradient(const typename VolumeType::Sampler& volIter)
	{
		const int32_t x = volIter.getPosition().getX();
		const int32_t y = volIter.getPosition().getY();
		const int32_t z = volIter.getPosition().getZ();

		//FIXME - bitwise way of doing this?
		typename VolumeType::VoxelType voxel1nx = volIter.getVoxelAt(x-2, y  ,z  ) > 0 ? 1: 0;
		typename VolumeType::VoxelType voxel1px = volIter.getVoxelAt(x-2, y  ,z  ) > 0 ? 1: 0;

		typename VolumeType::VoxelType voxel1ny = volIter.getVoxelAt(x  , y-2,z  ) > 0 ? 1: 0;
		typename VolumeType::VoxelType voxel1py = volIter.getVoxelAt(x  , y-2,z  ) > 0 ? 1: 0;

		typename VolumeType::VoxelType voxel1nz = volIter.getVoxelAt(x  , y  ,z-2) > 0 ? 1: 0;
		typename VolumeType::VoxelType voxel1pz = volIter.getVoxelAt(x  , y  ,z-2) > 0 ? 1: 0;

		return Vector3DFloat
		(
			static_cast<float>(voxel1nx) - static_cast<float>(voxel1px),
			static_cast<float>(voxel1ny) - static_cast<float>(voxel1py),
			static_cast<float>(voxel1nz) - static_cast<float>(voxel1pz)
		);
	}

	template<typename VolumeType>
	Vector3DFloat computeSmoothCentralDifferenceGradient(typename VolumeType::Sampler& volIter)
	{
		int32_t initialX = volIter.getPosition().getX();
		int32_t initialY = volIter.getPosition().getY();
		int32_t initialZ = volIter.getPosition().getZ();

		//FIXME - bitwise way of doing this?
		volIter.setPosition(initialX-1, initialY, initialZ);
		float voxel1nx = computeSmoothedVoxel(volIter);
		volIter.setPosition(initialX+1, initialY, initialZ);
		float voxel1px = computeSmoothedVoxel(volIter);

		volIter.setPosition(initialX, initialY-1, initialZ);
		float voxel1ny = computeSmoothedVoxel(volIter);
		volIter.setPosition(initialX, initialY+1, initialZ);
		float voxel1py = computeSmoothedVoxel(volIter);

		volIter.setPosition(initialX, initialY, initialZ-1);
		float voxel1nz = computeSmoothedVoxel(volIter);
		volIter.setPosition(initialX, initialY, initialZ+1);
		float voxel1pz = computeSmoothedVoxel(volIter);

		return Vector3DFloat
		(
			voxel1nx - voxel1px,
			voxel1ny - voxel1py,
			voxel1nz - voxel1pz
		);
	}

	template<typename VolumeType>
	Vector3DFloat computeSobelGradient(const typename VolumeType::Sampler& volIter)
	{
		static const int weights[3][3][3] = {  {  {2,3,2}, {3,6,3}, {2,3,2}  },  {
			{3,6,3},  {6,0,6},  {3,6,3} },  { {2,3,2},  {3,6,3},  {2,3,2} } };

			const typename VolumeType::VoxelType pVoxel1nx1ny1nz = volIter.peekVoxel1nx1ny1nz() > 0 ? 1: 0;
			const typename VolumeType::VoxelType pVoxel1nx1ny0pz = volIter.peekVoxel1nx1ny0pz() > 0 ? 1: 0;
			const typename VolumeType::VoxelType pVoxel1nx1ny1pz = volIter.peekVoxel1nx1ny1pz() > 0 ? 1: 0;
			const typename VolumeType::VoxelType pVoxel1nx0py1nz = volIter.peekVoxel1nx0py1nz() > 0 ? 1: 0;
			const typename VolumeType::VoxelType pVoxel1nx0py0pz = volIter.peekVoxel1nx0py0pz() > 0 ? 1: 0;
			const typename VolumeType::VoxelType pVoxel1nx0py1pz = volIter.peekVoxel1nx0py1pz() > 0 ? 1: 0;
			const typename VolumeType::VoxelType pVoxel1nx1py1nz = volIter.peekVoxel1nx1py1nz() > 0 ? 1: 0;
			const typename VolumeType::VoxelType pVoxel1nx1py0pz = volIter.peekVoxel1nx1py0pz() > 0 ? 1: 0;
			const typename VolumeType::VoxelType pVoxel1nx1py1pz = volIter.peekVoxel1nx1py1pz() > 0 ? 1: 0;

			const typename VolumeType::VoxelType pVoxel0px1ny1nz = volIter.peekVoxel0px1ny1nz() > 0 ? 1: 0;
			const typename VolumeType::VoxelType pVoxel0px1ny0pz = volIter.peekVoxel0px1ny0pz() > 0 ? 1: 0;
			const typename VolumeType::VoxelType pVoxel0px1ny1pz = volIter.peekVoxel0px1ny1pz() > 0 ? 1: 0;
			const typename VolumeType::VoxelType pVoxel0px0py1nz = volIter.peekVoxel0px0py1nz() > 0 ? 1: 0;
			//const VolumeType::VoxelType pVoxel0px0py0pz = volIter.peekVoxel0px0py0pz() > 0 ? 1: 0;
			const typename VolumeType::VoxelType pVoxel0px0py1pz = volIter.peekVoxel0px0py1pz() > 0 ? 1: 0;
			const typename VolumeType::VoxelType pVoxel0px1py1nz = volIter.peekVoxel0px1py1nz() > 0 ? 1: 0;
			const typename VolumeType::VoxelType pVoxel0px1py0pz = volIter.peekVoxel0px1py0pz() > 0 ? 1: 0;
			const typename VolumeType::VoxelType pVoxel0px1py1pz = volIter.peekVoxel0px1py1pz() > 0 ? 1: 0;

			const typename VolumeType::VoxelType pVoxel1px1ny1nz = volIter.peekVoxel1px1ny1nz() > 0 ? 1: 0;
			const typename VolumeType::VoxelType pVoxel1px1ny0pz = volIter.peekVoxel1px1ny0pz() > 0 ? 1: 0;
			const typename VolumeType::VoxelType pVoxel1px1ny1pz = volIter.peekVoxel1px1ny1pz() > 0 ? 1: 0;
			const typename VolumeType::VoxelType pVoxel1px0py1nz = volIter.peekVoxel1px0py1nz() > 0 ? 1: 0;
			const typename VolumeType::VoxelType pVoxel1px0py0pz = volIter.peekVoxel1px0py0pz() > 0 ? 1: 0;
			const typename VolumeType::VoxelType pVoxel1px0py1pz = volIter.peekVoxel1px0py1pz() > 0 ? 1: 0;
			const typename VolumeType::VoxelType pVoxel1px1py1nz = volIter.peekVoxel1px1py1nz() > 0 ? 1: 0;
			const typename VolumeType::VoxelType pVoxel1px1py0pz = volIter.peekVoxel1px1py0pz() > 0 ? 1: 0;
			const typename VolumeType::VoxelType pVoxel1px1py1pz = volIter.peekVoxel1px1py1pz() > 0 ? 1: 0;

			const int xGrad(- weights[0][0][0] * pVoxel1nx1ny1nz -
				weights[1][0][0] * pVoxel1nx1ny0pz - weights[2][0][0] *
				pVoxel1nx1ny1pz - weights[0][1][0] * pVoxel1nx0py1nz -
				weights[1][1][0] * pVoxel1nx0py0pz - weights[2][1][0] *
				pVoxel1nx0py1pz - weights[0][2][0] * pVoxel1nx1py1nz -
				weights[1][2][0] * pVoxel1nx1py0pz - weights[2][2][0] *
				pVoxel1nx1py1pz + weights[0][0][2] * pVoxel1px1ny1nz +
				weights[1][0][2] * pVoxel1px1ny0pz + weights[2][0][2] *
				pVoxel1px1ny1pz + weights[0][1][2] * pVoxel1px0py1nz +
				weights[1][1][2] * pVoxel1px0py0pz + weights[2][1][2] *
				pVoxel1px0py1pz + weights[0][2][2] * pVoxel1px1py1nz +
				weights[1][2][2] * pVoxel1px1py0pz + weights[2][2][2] *
				pVoxel1px1py1pz);

			const int yGrad(- weights[0][0][0] * pVoxel1nx1ny1nz -
				weights[1][0][0] * pVoxel1nx1ny0pz - weights[2][0][0] *
				pVoxel1nx1ny1pz + weights[0][2][0] * pVoxel1nx1py1nz +
				weights[1][2][0] * pVoxel1nx1py0pz + weights[2][2][0] *
				pVoxel1nx1py1pz - weights[0][0][1] * pVoxel0px1ny1nz -
				weights[1][0][1] * pVoxel0px1ny0pz - weights[2][0][1] *
				pVoxel0px1ny1pz + weights[0][2][1] * pVoxel0px1py1nz +
				weights[1][2][1] * pVoxel0px1py0pz + weights[2][2][1] *
				pVoxel0px1py1pz - weights[0][0][2] * pVoxel1px1ny1nz -
				weights[1][0][2] * pVoxel1px1ny0pz - weights[2][0][2] *
				pVoxel1px1ny1pz + weights[0][2][2] * pVoxel1px1py1nz +
				weights[1][2][2] * pVoxel1px1py0pz + weights[2][2][2] *
				pVoxel1px1py1pz);

			const int zGrad(- weights[0][0][0] * pVoxel1nx1ny1nz +
				weights[2][0][0] * pVoxel1nx1ny1pz - weights[0][1][0] *
				pVoxel1nx0py1nz + weights[2][1][0] * pVoxel1nx0py1pz -
				weights[0][2][0] * pVoxel1nx1py1nz + weights[2][2][0] *
				pVoxel1nx1py1pz - weights[0][0][1] * pVoxel0px1ny1nz +
				weights[2][0][1] * pVoxel0px1ny1pz - weights[0][1][1] *
				pVoxel0px0py1nz + weights[2][1][1] * pVoxel0px0py1pz -
				weights[0][2][1] * pVoxel0px1py1nz + weights[2][2][1] *
				pVoxel0px1py1pz - weights[0][0][2] * pVoxel1px1ny1nz +
				weights[2][0][2] * pVoxel1px1ny1pz - weights[0][1][2] *
				pVoxel1px0py1nz + weights[2][1][2] * pVoxel1px0py1pz -
				weights[0][2][2] * pVoxel1px1py1nz + weights[2][2][2] *
				pVoxel1px1py1pz);

			//Note: The above actually give gradients going from low density to high density.
			//For our normals we want the the other way around, so we switch the components as we return them.
			return Vector3DFloat(static_cast<float>(-xGrad),static_cast<float>(-yGrad),static_cast<float>(-zGrad));
	}

	template<typename VolumeType>
	Vector3DFloat computeSmoothSobelGradient(typename VolumeType::Sampler& volIter)
	{
		static const int weights[3][3][3] = {  {  {2,3,2}, {3,6,3}, {2,3,2}  },  {
			{3,6,3},  {6,0,6},  {3,6,3} },  { {2,3,2},  {3,6,3},  {2,3,2} } };

			int32_t initialX = volIter.getPosition().getX();
			int32_t initialY = volIter.getPosition().getY();
			int32_t initialZ = volIter.getPosition().getZ();

			volIter.setPosition(initialX-1, initialY-1, initialZ-1);	const float pVoxel1nx1ny1nz = computeSmoothedVoxel(volIter);
			volIter.setPosition(initialX-1, initialY-1, initialZ );		const float pVoxel1nx1ny0pz = computeSmoothedVoxel(volIter);
			volIter.setPosition(initialX-1, initialY-1, initialZ+1);	const float pVoxel1nx1ny1pz = computeSmoothedVoxel(volIter);
			volIter.setPosition(initialX-1, initialY  , initialZ-1);	const float pVoxel1nx0py1nz = computeSmoothedVoxel(volIter);
			volIter.setPosition(initialX-1, initialY  , initialZ );		const float pVoxel1nx0py0pz = computeSmoothedVoxel(volIter);
			volIter.setPosition(initialX-1, initialY  , initialZ+1);	const float pVoxel1nx0py1pz = computeSmoothedVoxel(volIter);
			volIter.setPosition(initialX-1, initialY+1, initialZ-1);	const float pVoxel1nx1py1nz = computeSmoothedVoxel(volIter);
			volIter.setPosition(initialX-1, initialY+1, initialZ );		const float pVoxel1nx1py0pz = computeSmoothedVoxel(volIter);
			volIter.setPosition(initialX-1, initialY+1, initialZ+1);	const float pVoxel1nx1py1pz = computeSmoothedVoxel(volIter);

			volIter.setPosition(initialX  , initialY-1, initialZ-1);	const float pVoxel0px1ny1nz = computeSmoothedVoxel(volIter);
			volIter.setPosition(initialX  , initialY-1, initialZ );		const float pVoxel0px1ny0pz = computeSmoothedVoxel(volIter);
			volIter.setPosition(initialX  , initialY-1, initialZ+1);	const float pVoxel0px1ny1pz = computeSmoothedVoxel(volIter);
			volIter.setPosition(initialX  , initialY  , initialZ-1);	const float pVoxel0px0py1nz = computeSmoothedVoxel(volIter);
			//volIter.setPosition(initialX  , initialY  , initialZ );		const float pVoxel0px0py0pz = computeSmoothedVoxel(volIter);
			volIter.setPosition(initialX  , initialY  , initialZ+1);	const float pVoxel0px0py1pz = computeSmoothedVoxel(volIter);
			volIter.setPosition(initialX  , initialY+1, initialZ-1);	const float pVoxel0px1py1nz = computeSmoothedVoxel(volIter);
			volIter.setPosition(initialX  , initialY+1, initialZ );		const float pVoxel0px1py0pz = computeSmoothedVoxel(volIter);
			volIter.setPosition(initialX  , initialY+1, initialZ+1);	const float pVoxel0px1py1pz = computeSmoothedVoxel(volIter);

			volIter.setPosition(initialX+1, initialY-1, initialZ-1);	const float pVoxel1px1ny1nz = computeSmoothedVoxel(volIter);
			volIter.setPosition(initialX+1, initialY-1, initialZ );		const float pVoxel1px1ny0pz = computeSmoothedVoxel(volIter);
			volIter.setPosition(initialX+1, initialY-1, initialZ+1);	const float pVoxel1px1ny1pz = computeSmoothedVoxel(volIter);
			volIter.setPosition(initialX+1, initialY  , initialZ-1);	const float pVoxel1px0py1nz = computeSmoothedVoxel(volIter);
			volIter.setPosition(initialX+1, initialY  , initialZ );		const float pVoxel1px0py0pz = computeSmoothedVoxel(volIter);
			volIter.setPosition(initialX+1, initialY  , initialZ+1);	const float pVoxel1px0py1pz = computeSmoothedVoxel(volIter);
			volIter.setPosition(initialX+1, initialY+1, initialZ-1);	const float pVoxel1px1py1nz = computeSmoothedVoxel(volIter);
			volIter.setPosition(initialX+1, initialY+1, initialZ );		const float pVoxel1px1py0pz = computeSmoothedVoxel(volIter);
			volIter.setPosition(initialX+1, initialY+1, initialZ+1);	const float pVoxel1px1py1pz = computeSmoothedVoxel(volIter);

			/*const VoxelType pVoxel1nx1ny1nz = volIter.peekVoxel1nx1ny1nz() > 0 ? 1: 0;
			const VoxelType pVoxel1nx1ny0pz = volIter.peekVoxel1nx1ny0pz() > 0 ? 1: 0;
			const VoxelType pVoxel1nx1ny1pz = volIter.peekVoxel1nx1ny1pz() > 0 ? 1: 0;
			const VoxelType pVoxel1nx0py1nz = volIter.peekVoxel1nx0py1nz() > 0 ? 1: 0;
			const VoxelType pVoxel1nx0py0pz = volIter.peekVoxel1nx0py0pz() > 0 ? 1: 0;
			const VoxelType pVoxel1nx0py1pz = volIter.peekVoxel1nx0py1pz() > 0 ? 1: 0;
			const VoxelType pVoxel1nx1py1nz = volIter.peekVoxel1nx1py1nz() > 0 ? 1: 0;
			const VoxelType pVoxel1nx1py0pz = volIter.peekVoxel1nx1py0pz() > 0 ? 1: 0;
			const VoxelType pVoxel1nx1py1pz = volIter.peekVoxel1nx1py1pz() > 0 ? 1: 0;

			const VoxelType pVoxel0px1ny1nz = volIter.peekVoxel0px1ny1nz() > 0 ? 1: 0;
			const VoxelType pVoxel0px1ny0pz = volIter.peekVoxel0px1ny0pz() > 0 ? 1: 0;
			const VoxelType pVoxel0px1ny1pz = volIter.peekVoxel0px1ny1pz() > 0 ? 1: 0;
			const VoxelType pVoxel0px0py1nz = volIter.peekVoxel0px0py1nz() > 0 ? 1: 0;
			//const VoxelType pVoxel0px0py0pz = volIter.peekVoxel0px0py0pz() > 0 ? 1: 0;
			const VoxelType pVoxel0px0py1pz = volIter.peekVoxel0px0py1pz() > 0 ? 1: 0;
			const VoxelType pVoxel0px1py1nz = volIter.peekVoxel0px1py1nz() > 0 ? 1: 0;
			const VoxelType pVoxel0px1py0pz = volIter.peekVoxel0px1py0pz() > 0 ? 1: 0;
			const VoxelType pVoxel0px1py1pz = volIter.peekVoxel0px1py1pz() > 0 ? 1: 0;

			const VoxelType pVoxel1px1ny1nz = volIter.peekVoxel1px1ny1nz() > 0 ? 1: 0;
			const VoxelType pVoxel1px1ny0pz = volIter.peekVoxel1px1ny0pz() > 0 ? 1: 0;
			const VoxelType pVoxel1px1ny1pz = volIter.peekVoxel1px1ny1pz() > 0 ? 1: 0;
			const VoxelType pVoxel1px0py1nz = volIter.peekVoxel1px0py1nz() > 0 ? 1: 0;
			const VoxelType pVoxel1px0py0pz = volIter.peekVoxel1px0py0pz() > 0 ? 1: 0;
			const VoxelType pVoxel1px0py1pz = volIter.peekVoxel1px0py1pz() > 0 ? 1: 0;
			const VoxelType pVoxel1px1py1nz = volIter.peekVoxel1px1py1nz() > 0 ? 1: 0;
			const VoxelType pVoxel1px1py0pz = volIter.peekVoxel1px1py0pz() > 0 ? 1: 0;
			const VoxelType pVoxel1px1py1pz = volIter.peekVoxel1px1py1pz() > 0 ? 1: 0;*/

			const float xGrad(- weights[0][0][0] * pVoxel1nx1ny1nz -
				weights[1][0][0] * pVoxel1nx1ny0pz - weights[2][0][0] *
				pVoxel1nx1ny1pz - weights[0][1][0] * pVoxel1nx0py1nz -
				weights[1][1][0] * pVoxel1nx0py0pz - weights[2][1][0] *
				pVoxel1nx0py1pz - weights[0][2][0] * pVoxel1nx1py1nz -
				weights[1][2][0] * pVoxel1nx1py0pz - weights[2][2][0] *
				pVoxel1nx1py1pz + weights[0][0][2] * pVoxel1px1ny1nz +
				weights[1][0][2] * pVoxel1px1ny0pz + weights[2][0][2] *
				pVoxel1px1ny1pz + weights[0][1][2] * pVoxel1px0py1nz +
				weights[1][1][2] * pVoxel1px0py0pz + weights[2][1][2] *
				pVoxel1px0py1pz + weights[0][2][2] * pVoxel1px1py1nz +
				weights[1][2][2] * pVoxel1px1py0pz + weights[2][2][2] *
				pVoxel1px1py1pz);

			const float yGrad(- weights[0][0][0] * pVoxel1nx1ny1nz -
				weights[1][0][0] * pVoxel1nx1ny0pz - weights[2][0][0] *
				pVoxel1nx1ny1pz + weights[0][2][0] * pVoxel1nx1py1nz +
				weights[1][2][0] * pVoxel1nx1py0pz + weights[2][2][0] *
				pVoxel1nx1py1pz - weights[0][0][1] * pVoxel0px1ny1nz -
				weights[1][0][1] * pVoxel0px1ny0pz - weights[2][0][1] *
				pVoxel0px1ny1pz + weights[0][2][1] * pVoxel0px1py1nz +
				weights[1][2][1] * pVoxel0px1py0pz + weights[2][2][1] *
				pVoxel0px1py1pz - weights[0][0][2] * pVoxel1px1ny1nz -
				weights[1][0][2] * pVoxel1px1ny0pz - weights[2][0][2] *
				pVoxel1px1ny1pz + weights[0][2][2] * pVoxel1px1py1nz +
				weights[1][2][2] * pVoxel1px1py0pz + weights[2][2][2] *
				pVoxel1px1py1pz);

			const float zGrad(- weights[0][0][0] * pVoxel1nx1ny1nz +
				weights[2][0][0] * pVoxel1nx1ny1pz - weights[0][1][0] *
				pVoxel1nx0py1nz + weights[2][1][0] * pVoxel1nx0py1pz -
				weights[0][2][0] * pVoxel1nx1py1nz + weights[2][2][0] *
				pVoxel1nx1py1pz - weights[0][0][1] * pVoxel0px1ny1nz +
				weights[2][0][1] * pVoxel0px1ny1pz - weights[0][1][1] *
				pVoxel0px0py1nz + weights[2][1][1] * pVoxel0px0py1pz -
				weights[0][2][1] * pVoxel0px1py1nz + weights[2][2][1] *
				pVoxel0px1py1pz - weights[0][0][2] * pVoxel1px1ny1nz +
				weights[2][0][2] * pVoxel1px1ny1pz - weights[0][1][2] *
				pVoxel1px0py1nz + weights[2][1][2] * pVoxel1px0py1pz -
				weights[0][2][2] * pVoxel1px1py1nz + weights[2][2][2] *
				pVoxel1px1py1pz);

			//Note: The above actually give gradients going from low density to high density.
			//For our normals we want the the other way around, so we switch the components as we return them.
			return Vector3DFloat(-xGrad,-yGrad,-zGrad);
	}
}
