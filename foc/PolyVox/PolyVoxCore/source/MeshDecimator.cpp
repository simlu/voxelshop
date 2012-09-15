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

#include "PolyVoxCore/MeshDecimator.h"
#include "PolyVoxCore/SurfaceMesh.h"

using namespace std;

namespace PolyVox
{
	template<>
	POLYVOX_API void MeshDecimator<PositionMaterial>::fillInitialVertexMetadata(std::vector<InitialVertexMetadata>& vecVertexMetadata)
	{
		vecVertexMetadata.clear();
		vecVertexMetadata.resize(m_pOutputMesh->m_vecVertices.size());
		//Initialise the metadata
		for(uint32_t ct = 0; ct < vecVertexMetadata.size(); ct++)
		{
			vecVertexMetadata[ct].normal.setElements(0,0,0);
			vecVertexMetadata[ct].isOnMaterialEdge = false;
			vecVertexMetadata[ct].isOnRegionFace.reset();
		}

		//Identify duplicate vertices, as they lie on the material edge. To do this we convert into integers and sort 
		//(first on z, then y, then x). They should be mostly in order as this is the order they come out of the
		//CubicSurfaceExtractor in. Duplicates are now neighbours in the resulting list so just scan through for pairs.
		std::vector<IntVertex> intVertices;
		intVertices.reserve(m_pOutputMesh->m_vecVertices.size());
		for(uint32_t ct = 0; ct < m_pOutputMesh->m_vecVertices.size(); ct++)
		{
			const Vector3DFloat& floatPos = m_pOutputMesh->m_vecVertices[ct].position;
			IntVertex intVertex(static_cast<uint32_t>(floatPos.getX()), static_cast<uint32_t>(floatPos.getY()), static_cast<uint32_t>(floatPos.getZ()), ct);
			intVertices.push_back(intVertex);
		}

		//Do the sorting so that duplicate become neighbours
		sort(intVertices.begin(), intVertices.end());

		//Find neighbours which are duplicates.
		for(uint32_t ct = 0; ct < intVertices.size() - 1; ct++)
		{
			const IntVertex& v0 = intVertices[ct+0];
			const IntVertex& v1 = intVertices[ct+1];

			if((v0.x == v1.x) && (v0.y == v1.y) && (v0.z == v1.z))
			{
				vecVertexMetadata[v0.index].isOnMaterialEdge = true;
				vecVertexMetadata[v1.index].isOnMaterialEdge = true;
			}
		}

		//Compute an approcimation to the normal, used when deciding if an edge can collapse.
		for(uint32_t ct = 0; ct < m_pOutputMesh->m_vecVertices.size(); ct++)
		{
			Vector3DFloat sumOfNormals(0.0f,0.0f,0.0f);
			for(vector<uint32_t>::iterator iter = trianglesUsingVertex[ct].begin(); iter != trianglesUsingVertex[ct].end(); iter++)
			{
				sumOfNormals += m_vecTriangles[*iter].normal;
			}

			vecVertexMetadata[ct].normal = sumOfNormals;
			vecVertexMetadata[ct].normal.normalise();
		}

		//Identify those vertices on the edge of a region. Care will need to be taken when moving them.
		for(uint32_t ct = 0; ct < vecVertexMetadata.size(); ct++)
		{
			Region regTransformed = m_pOutputMesh->m_Region;
			regTransformed.shift(regTransformed.getLowerCorner() * static_cast<int32_t>(-1));

			//Plus and minus X
			vecVertexMetadata[ct].isOnRegionFace.set(RFF_ON_REGION_FACE_NEG_X, m_pOutputMesh->m_vecVertices[ct].getPosition().getX() < regTransformed.getLowerCorner().getX() + 0.001f);
			vecVertexMetadata[ct].isOnRegionFace.set(RFF_ON_REGION_FACE_POS_X, m_pOutputMesh->m_vecVertices[ct].getPosition().getX() > regTransformed.getUpperCorner().getX() - 0.001f);
			//Plus and minus Y
			vecVertexMetadata[ct].isOnRegionFace.set(RFF_ON_REGION_FACE_NEG_Y, m_pOutputMesh->m_vecVertices[ct].getPosition().getY() < regTransformed.getLowerCorner().getY() + 0.001f);
			vecVertexMetadata[ct].isOnRegionFace.set(RFF_ON_REGION_FACE_POS_Y, m_pOutputMesh->m_vecVertices[ct].getPosition().getY() > regTransformed.getUpperCorner().getY() - 0.001f);
			//Plus and minus Z
			vecVertexMetadata[ct].isOnRegionFace.set(RFF_ON_REGION_FACE_NEG_Z, m_pOutputMesh->m_vecVertices[ct].getPosition().getZ() < regTransformed.getLowerCorner().getZ() + 0.001f);
			vecVertexMetadata[ct].isOnRegionFace.set(RFF_ON_REGION_FACE_POS_Z, m_pOutputMesh->m_vecVertices[ct].getPosition().getZ() > regTransformed.getUpperCorner().getZ() - 0.001f);
		}
	}

	template<>
	POLYVOX_API void MeshDecimator<PositionMaterialNormal>::fillInitialVertexMetadata(std::vector<InitialVertexMetadata>& vecVertexMetadata)
	{
		vecVertexMetadata.clear();
		vecVertexMetadata.resize(m_pOutputMesh->m_vecVertices.size());

		//Initialise the metadata
		for(uint32_t ct = 0; ct < vecVertexMetadata.size(); ct++)
		{			
			vecVertexMetadata[ct].isOnRegionFace.reset();
			vecVertexMetadata[ct].isOnMaterialEdge = false;
			vecVertexMetadata[ct].normal = m_pOutputMesh->m_vecVertices[ct].normal;
		}

		//Identify those vertices on the edge of a region. Care will need to be taken when moving them.
		for(uint32_t ct = 0; ct < vecVertexMetadata.size(); ct++)
		{
			Region regTransformed = m_pOutputMesh->m_Region;
			regTransformed.shift(regTransformed.getLowerCorner() * static_cast<int32_t>(-1));

			//Plus and minus X
			vecVertexMetadata[ct].isOnRegionFace.set(RFF_ON_REGION_FACE_NEG_X, m_pOutputMesh->m_vecVertices[ct].getPosition().getX() < regTransformed.getLowerCorner().getX() + 0.001f);
			vecVertexMetadata[ct].isOnRegionFace.set(RFF_ON_REGION_FACE_POS_X, m_pOutputMesh->m_vecVertices[ct].getPosition().getX() > regTransformed.getUpperCorner().getX() - 0.001f);
			//Plus and minus Y
			vecVertexMetadata[ct].isOnRegionFace.set(RFF_ON_REGION_FACE_NEG_Y, m_pOutputMesh->m_vecVertices[ct].getPosition().getY() < regTransformed.getLowerCorner().getY() + 0.001f);
			vecVertexMetadata[ct].isOnRegionFace.set(RFF_ON_REGION_FACE_POS_Y, m_pOutputMesh->m_vecVertices[ct].getPosition().getY() > regTransformed.getUpperCorner().getY() - 0.001f);
			//Plus and minus Z
			vecVertexMetadata[ct].isOnRegionFace.set(RFF_ON_REGION_FACE_NEG_Z, m_pOutputMesh->m_vecVertices[ct].getPosition().getZ() < regTransformed.getLowerCorner().getZ() + 0.001f);
			vecVertexMetadata[ct].isOnRegionFace.set(RFF_ON_REGION_FACE_POS_Z, m_pOutputMesh->m_vecVertices[ct].getPosition().getZ() > regTransformed.getUpperCorner().getZ() - 0.001f);
		}

		//If all three vertices have the same material then we are not on a material edge. If any vertex has a different
		//material then all three vertices are on a material edge. E.g. If one vertex has material 'a' and the other two 
		//have material 'b', then the two 'b's are still on an edge (with 'a') even though they are the same as eachother.
		for(uint32_t ct = 0; ct < m_vecTriangles.size(); ct++)
		{
			uint32_t v0 = m_vecTriangles[ct].v0;
			uint32_t v1 = m_vecTriangles[ct].v1;
			uint32_t v2 = m_vecTriangles[ct].v2;

			bool allMatch = 
				(m_pOutputMesh->m_vecVertices[v0].material == m_pOutputMesh->m_vecVertices[v1].material) && 
				(m_pOutputMesh->m_vecVertices[v1].material == m_pOutputMesh->m_vecVertices[v2].material);

			if(!allMatch)
			{
				vecVertexMetadata[v0].isOnMaterialEdge = true;
				vecVertexMetadata[v1].isOnMaterialEdge = true;
				vecVertexMetadata[v2].isOnMaterialEdge = true;
			}
		}
	}

	template<> 
	POLYVOX_API bool MeshDecimator<PositionMaterialNormal>::canCollapseNormalEdge(uint32_t uSrc, uint32_t uDst)
	{
		if(m_vecInitialVertexMetadata[uSrc].normal.dot(m_vecInitialVertexMetadata[uDst].normal) < m_fMinDotProductForCollapse)
		{
			return false;
		}

		//With the marching cubes surface we honour the user specified threshold
		return !collapseChangesFaceNormals(uSrc, uDst, m_fMinDotProductForCollapse);
	}

	template<> 
	POLYVOX_API bool MeshDecimator<PositionMaterial>::canCollapseNormalEdge(uint32_t uSrc, uint32_t uDst)
	{
		//We don't actually use the normal here, because we want to allow face
		//vertices to collapse onto edge vertices. Simply checking whether anything
		//has flipped has proved to be the most robust approach, though rather slow.
		//It's not sufficient to just check the normals, there can be holes in the middle
		//of the mesh for example.

		//User specified threshold is not used for cubic surface, any
		//movement is too much (but allow for floating point error).
		return !collapseChangesFaceNormals(uSrc, uDst, 0.999f);
	}
}
