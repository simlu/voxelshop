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

#include "PolyVoxImpl/TypeDef.h"

#include "PolyVoxCore/GradientEstimators.h"

using namespace std;

namespace PolyVox
{
	/*void computeNormalsForVertices(LargeVolume<uint8_t>* volumeData, SurfaceMesh<PositionMaterialNormal>& mesh, NormalGenerationMethod normalGenerationMethod)
	{
		std::vector<PositionMaterialNormal>& vecVertices = mesh.getRawVertexData();
		std::vector<PositionMaterialNormal>::iterator iterSurfaceVertex = vecVertices.begin();
		while(iterSurfaceVertex != vecVertices.end())
		{
			const Vector3DFloat& v3dPos = iterSurfaceVertex->getPosition() + static_cast<Vector3DFloat>(mesh.m_Region.getLowerCorner());
			const Vector3DInt32 v3dFloor = static_cast<Vector3DInt32>(v3dPos);

			LargeVolume<uint8_t>::Sampler volIter(volumeData);

			//Check all corners are within the volume, allowing a boundary for gradient estimation
			bool lowerCornerInside = volumeData->getEnclosingRegion().containsPoint(v3dFloor,2);
			bool upperCornerInside = volumeData->getEnclosingRegion().containsPoint(v3dFloor+Vector3DInt32(1,1,1),2);

			if(lowerCornerInside && upperCornerInside) //If this test fails the vertex will be left as it was
			{
				Vector3DFloat v3dGradient = computeNormal(volumeData, v3dPos, normalGenerationMethod);
				
				if(v3dGradient.lengthSquared() > 0.0001)
				{
					//If we got a normal of significant length then update it.
					//Otherwise leave it as it was (should be the 'simple' version)
					v3dGradient.normalise();
					iterSurfaceVertex->setNormal(v3dGradient);
				}
			} //(lowerCornerInside && upperCornerInside)
			++iterSurfaceVertex;
		}
	}*/

	/*Vector3DFloat computeNormal(LargeVolume<uint8_t>* volumeData, const Vector3DFloat& v3dPos, NormalGenerationMethod normalGenerationMethod)
	{
		Vector3DFloat v3dGradient; //To store the result

		LargeVolume<uint8_t>::Sampler volIter(volumeData);

			const Vector3DInt32 v3dFloor = static_cast<Vector3DInt32>(v3dPos);

			volIter.setPosition(static_cast<Vector3DInt32>(v3dFloor));
			Vector3DFloat gradFloor;
			switch(normalGenerationMethod)
			{
			case SOBEL_SMOOTHED:
				gradFloor = computeSmoothSobelGradient<uint8_t>(volIter);
				break;
			case CENTRAL_DIFFERENCE_SMOOTHED:
				gradFloor = computeSmoothCentralDifferenceGradient<uint8_t>(volIter);
				break;
			case SOBEL:
				gradFloor = computeSobelGradient<uint8_t>(volIter);
				break;
			case CENTRAL_DIFFERENCE:
				gradFloor = computeCentralDifferenceGradient<uint8_t>(volIter);
				break;
			}

			if((v3dPos.getX() - v3dFloor.getX()) > 0.25) //The result should be 0.0 or 0.5
			{			
				volIter.setPosition(static_cast<Vector3DInt32>(v3dFloor+Vector3DInt32(1,0,0)));
			}
			if((v3dPos.getY() - v3dFloor.getY()) > 0.25) //The result should be 0.0 or 0.5
			{			
				volIter.setPosition(static_cast<Vector3DInt32>(v3dFloor+Vector3DInt32(0,1,0)));
			}
			if((v3dPos.getZ() - v3dFloor.getZ()) > 0.25) //The result should be 0.0 or 0.5
			{			
				volIter.setPosition(static_cast<Vector3DInt32>(v3dFloor+Vector3DInt32(0,0,1)));					
			}

			Vector3DFloat gradCeil;
			switch(normalGenerationMethod)
			{
			case SOBEL_SMOOTHED:
				gradCeil = computeSmoothSobelGradient<uint8_t>(volIter);
				break;
			case CENTRAL_DIFFERENCE_SMOOTHED:
				gradCeil = computeSmoothCentralDifferenceGradient<uint8_t>(volIter);
				break;
			case SOBEL:
				gradCeil = computeSobelGradient<uint8_t>(volIter);
				break;
			case CENTRAL_DIFFERENCE:
				gradCeil = computeCentralDifferenceGradient<uint8_t>(volIter);
				break;
			}

			v3dGradient = (gradFloor + gradCeil);
			if(v3dGradient.lengthSquared() < 0.0001)
			{
				//Operation failed - fall back on simple gradient estimation
				normalGenerationMethod = SIMPLE;
			}

			if(normalGenerationMethod == SIMPLE)
			{
				volIter.setPosition(static_cast<Vector3DInt32>(v3dFloor));
				const uint8_t uFloor = volIter.getVoxel() > 0 ? 1 : 0;
				if((v3dPos.getX() - v3dFloor.getX()) > 0.25) //The result should be 0.0 or 0.5
				{					
					uint8_t uCeil = volIter.peekVoxel1px0py0pz() > 0 ? 1 : 0;
					v3dGradient = Vector3DFloat(static_cast<float>(uFloor - uCeil),0.0,0.0);
				}
				else if((v3dPos.getY() - v3dFloor.getY()) > 0.25) //The result should be 0.0 or 0.5
				{
					uint8_t uCeil = volIter.peekVoxel0px1py0pz() > 0 ? 1 : 0;
					v3dGradient = Vector3DFloat(0.0,static_cast<float>(uFloor - uCeil),0.0);
				}
				else if((v3dPos.getZ() - v3dFloor.getZ()) > 0.25) //The result should be 0.0 or 0.5
				{
					uint8_t uCeil = volIter.peekVoxel0px0py1pz() > 0 ? 1 : 0;
					v3dGradient = Vector3DFloat(0.0, 0.0,static_cast<float>(uFloor - uCeil));					
				}
			}
			return v3dGradient;
	}*/
}
