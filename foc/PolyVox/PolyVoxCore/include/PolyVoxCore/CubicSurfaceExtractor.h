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

#ifndef __PolyVox_CubicSurfaceExtractor_H__
#define __PolyVox_CubicSurfaceExtractor_H__

#include "PolyVoxImpl/TypeDef.h"

#include "PolyVoxCore/Array.h"
#include "PolyVoxCore/DefaultIsQuadNeeded.h"
#include "PolyVoxCore/SurfaceMesh.h"

namespace PolyVox
{
	template<typename VolumeType, typename IsQuadNeeded = DefaultIsQuadNeeded<typename VolumeType::VoxelType> >
	class CubicSurfaceExtractor
	{
		struct IndexAndMaterial
		{
			int32_t iIndex;
			int32_t uMaterial; //Should actually use the material type here, but this is ok for now.
		};

		enum FaceNames
		{
			PositiveX,
			PositiveY,
			PositiveZ,
			NegativeX,
			NegativeY,
			NegativeZ,
			NoOfFaces
		};

		struct Quad
		{
			Quad(uint32_t v0, uint32_t v1, uint32_t v2, uint32_t v3)
			{
				vertices[0] = v0;
				vertices[1] = v1;
				vertices[2] = v2;
				vertices[3] = v3;
			}

			uint32_t vertices[4];
		};

	public:
		CubicSurfaceExtractor(VolumeType* volData, Region region, SurfaceMesh<PositionMaterial>* result, bool bMergeQuads = true, IsQuadNeeded isQuadNeeded = IsQuadNeeded());

		void execute();		

	private:
		int32_t addVertex(float fX, float fY, float fZ, uint32_t uMaterial, Array<3, IndexAndMaterial>& existingVertices);
		bool performQuadMerging(std::list<Quad>& quads);
		bool mergeQuads(Quad& q1, Quad& q2);

		IsQuadNeeded m_funcIsQuadNeededCallback;

		//The volume data and a sampler to access it.
		VolumeType* m_volData;

		//Information about the region we are currently processing
		Region m_regSizeInVoxels;

		//The surface patch we are currently filling.
		SurfaceMesh<PositionMaterial>* m_meshCurrent;

		//Used to avoid creating duplicate vertices.
		Array<3, IndexAndMaterial> m_previousSliceVertices;
		Array<3, IndexAndMaterial> m_currentSliceVertices;

		//During extraction we create a number of different lists of quads. All the 
		//quads in a given list are in the same plane and facing in the same direction.
		std::vector< std::list<Quad> > m_vecQuads[NoOfFaces];

		//Controls whether quad merging should be performed. This might be undesirable
		//is the user needs per-vertex attributes, or to perform per vertex lighting.
		bool m_bMergeQuads;

		//This constant defines the maximum number of quads which can share a
		//vertex in a cubic style mesh. See the initialisation for more details.
		static const uint32_t MaxVerticesPerPosition;
	};
}

#include "PolyVoxCore/CubicSurfaceExtractor.inl"

#endif
