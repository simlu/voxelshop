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
	AmbientOcclusionCalculator<VolumeType>::AmbientOcclusionCalculator(VolumeType* volInput, Array<3, uint8_t>* arrayResult, Region region, float fRayLength, uint8_t uNoOfSamplesPerOutputElement, polyvox_function<bool(const typename VolumeType::VoxelType& voxel)> funcIsTransparent)
		:m_region(region)
		,m_sampVolume(volInput)
		,m_volInput(volInput)
		,m_arrayResult(arrayResult)
		,m_fRayLength(fRayLength)
		,m_uNoOfSamplesPerOutputElement(uNoOfSamplesPerOutputElement)
		,mRandomUnitVectorIndex(0) //Although these could be uninitialised, we 
		,mRandomVectorIndex(0) //initialise for consistant results in the tests.
		,m_funcIsTransparent(funcIsTransparent)
	{
		//Make sure that the size of the volume is an exact multiple of the size of the array.
		assert(m_volInput->getWidth() % arrayResult->getDimension(0) == 0);
		assert(m_volInput->getHeight() % arrayResult->getDimension(1) == 0);
		assert(m_volInput->getDepth() % arrayResult->getDimension(2) == 0);

		//Our initial indices. It doesn't matter exactly what we set here, but the code below makes 
		//sure they are different for different regions which helps reduce tiling patterns in the results.
		mRandomUnitVectorIndex += m_region.getLowerCorner().getX() + m_region.getLowerCorner().getY() + m_region.getLowerCorner().getZ();
		mRandomVectorIndex += m_region.getLowerCorner().getX() + m_region.getLowerCorner().getY() + m_region.getLowerCorner().getZ();

		//This value helps us jump around in the array a bit more, so the
		//nth 'random' value isn't always followed by the n+1th 'random' value.
		mIndexIncreament = 1;
	}

	template<typename VolumeType>
	AmbientOcclusionCalculator<VolumeType>::~AmbientOcclusionCalculator()
	{
	}

	template<typename VolumeType>
	void AmbientOcclusionCalculator<VolumeType>::execute(void)
	{
		const int iRatioX = m_volInput->getWidth()  / m_arrayResult->getDimension(0);
		const int iRatioY = m_volInput->getHeight() / m_arrayResult->getDimension(1);
		const int iRatioZ = m_volInput->getDepth()  / m_arrayResult->getDimension(2);

		const float fRatioX = iRatioX;
		const float fRatioY = iRatioY;
		const float fRatioZ = iRatioZ;
		const Vector3DFloat v3dRatio(fRatioX, fRatioY, fRatioZ);

		const float fHalfRatioX = fRatioX * 0.5f;
		const float fHalfRatioY = fRatioY * 0.5f;
		const float fHalfRatioZ = fRatioZ * 0.5f;
		const Vector3DFloat v3dHalfRatio(fHalfRatioX, fHalfRatioY, fHalfRatioZ);

		const Vector3DFloat v3dOffset(0.5f,0.5f,0.5f);

		RaycastResult raycastResult;
		Raycast<VolumeType> raycast(m_volInput, Vector3DFloat(0.0f,0.0f,0.0f), Vector3DFloat(1.0f,1.0f,1.0f), raycastResult, polyvox_bind(&PolyVox::AmbientOcclusionCalculator<VolumeType>::raycastCallback, this, std::placeholders::_1));

		//This loop iterates over the bottom-lower-left voxel in each of the cells in the output array
		for(uint16_t z = m_region.getLowerCorner().getZ(); z <= m_region.getUpperCorner().getZ(); z += iRatioZ)
		{
			for(uint16_t y = m_region.getLowerCorner().getY(); y <= m_region.getUpperCorner().getY(); y += iRatioY)
			{
				for(uint16_t x = m_region.getLowerCorner().getX(); x <= m_region.getUpperCorner().getX(); x += iRatioX)
				{
					//Compute a start position corresponding to 
					//the centre of the cell in the output array.
					Vector3DFloat v3dStart(x, y, z);
					v3dStart -= v3dOffset;
					v3dStart += v3dHalfRatio;

					//Keep track of how many rays did not hit anything
					uint8_t uVisibleDirections = 0;

					for(int ct = 0; ct < m_uNoOfSamplesPerOutputElement; ct++)
					{						
						//We take a random vector with components going from -1 to 1 and scale it to go from -halfRatio to +halfRatio.
						//This jitter value moves our sample point from the center of the array cell to somewhere else in the array cell
						Vector3DFloat v3dJitter = randomVectors[(mRandomVectorIndex += (++mIndexIncreament)) % 1019]; //Prime number helps avoid repetition on sucessive loops.
						v3dJitter *= v3dHalfRatio;
						const Vector3DFloat v3dRayStart = v3dStart + v3dJitter;

						Vector3DFloat v3dRayDirection = randomUnitVectors[(mRandomUnitVectorIndex += (++mIndexIncreament)) % 1021]; //Differenct prime number.
						v3dRayDirection *= m_fRayLength;
						
						raycast.setStart(v3dRayStart);
						raycast.setDirection(v3dRayDirection);
						raycast.execute();

						if(raycastResult.foundIntersection == false)
						{
							++uVisibleDirections;
						}
					}

					float fVisibility;
					if(m_uNoOfSamplesPerOutputElement == 0)
					{
						//The user might request zero samples (I've done this in the past while debugging - I don't want to
						//wait for ambient occlusion but I do want as valid result for rendering). Avoid the divide by zero.
						fVisibility = 1.0f;
					}
					else
					{
						fVisibility = static_cast<float>(uVisibleDirections) / static_cast<float>(m_uNoOfSamplesPerOutputElement);
						assert((fVisibility >= 0.0f) && (fVisibility <= 1.0f));
					}

					(*m_arrayResult)[z / iRatioZ][y / iRatioY][x / iRatioX] = static_cast<uint8_t>(255.0f * fVisibility);
				}
			}
		}
	}

	template<typename VolumeType>
	bool AmbientOcclusionCalculator<VolumeType>::raycastCallback(const typename VolumeType::Sampler& sampler)
	{
		typename VolumeType::VoxelType voxel = sampler.getVoxel();
		return m_funcIsTransparent(voxel);
	}
}
