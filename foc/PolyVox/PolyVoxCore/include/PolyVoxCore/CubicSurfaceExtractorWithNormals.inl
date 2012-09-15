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
	template<typename VolumeType, typename IsQuadNeeded>
	CubicSurfaceExtractorWithNormals<VolumeType, IsQuadNeeded>::CubicSurfaceExtractorWithNormals(VolumeType* volData, Region region, SurfaceMesh<PositionMaterialNormal>* result, IsQuadNeeded isQuadNeeded)
		:m_volData(volData)
		,m_sampVolume(volData)
		,m_meshCurrent(result)
		,m_regSizeInVoxels(region)
	{		
		m_funcIsQuadNeededCallback = isQuadNeeded;
	}

	template<typename VolumeType, typename IsQuadNeeded>
	void CubicSurfaceExtractorWithNormals<VolumeType, IsQuadNeeded>::execute()
	{		
		m_meshCurrent->clear();

		for(int32_t z = m_regSizeInVoxels.getLowerCorner().getZ(); z < m_regSizeInVoxels.getUpperCorner().getZ(); z++)
		{
			for(int32_t y = m_regSizeInVoxels.getLowerCorner().getY(); y < m_regSizeInVoxels.getUpperCorner().getY(); y++)
			{
				for(int32_t x = m_regSizeInVoxels.getLowerCorner().getX(); x < m_regSizeInVoxels.getUpperCorner().getX(); x++)
				{
					// these are always positive anyway
					float regX = static_cast<float>(x - m_regSizeInVoxels.getLowerCorner().getX());
					float regY = static_cast<float>(y - m_regSizeInVoxels.getLowerCorner().getY());
					float regZ = static_cast<float>(z - m_regSizeInVoxels.getLowerCorner().getZ());

					float material = 0.0f;

					if(m_funcIsQuadNeededCallback(m_volData->getVoxelAt(x,y,z), m_volData->getVoxelAt(x+1,y,z), material))
					{
						uint32_t v0 = m_meshCurrent->addVertex(PositionMaterialNormal(Vector3DFloat(regX + 0.5f, regY - 0.5f, regZ - 0.5f), Vector3DFloat(1.0f, 0.0f, 0.0f), material));
						uint32_t v1 = m_meshCurrent->addVertex(PositionMaterialNormal(Vector3DFloat(regX + 0.5f, regY - 0.5f, regZ + 0.5f), Vector3DFloat(1.0f, 0.0f, 0.0f), material));
						uint32_t v2 = m_meshCurrent->addVertex(PositionMaterialNormal(Vector3DFloat(regX + 0.5f, regY + 0.5f, regZ - 0.5f), Vector3DFloat(1.0f, 0.0f, 0.0f), material));
						uint32_t v3 = m_meshCurrent->addVertex(PositionMaterialNormal(Vector3DFloat(regX + 0.5f, regY + 0.5f, regZ + 0.5f), Vector3DFloat(1.0f, 0.0f, 0.0f), material));

						m_meshCurrent->addTriangleCubic(v0,v2,v1);
						m_meshCurrent->addTriangleCubic(v1,v2,v3);
					}
					if(m_funcIsQuadNeededCallback(m_volData->getVoxelAt(x+1,y,z), m_volData->getVoxelAt(x,y,z), material))
					{
						uint32_t v0 = m_meshCurrent->addVertex(PositionMaterialNormal(Vector3DFloat(regX + 0.5f, regY - 0.5f, regZ - 0.5f), Vector3DFloat(-1.0f, 0.0f, 0.0f), material));
						uint32_t v1 = m_meshCurrent->addVertex(PositionMaterialNormal(Vector3DFloat(regX + 0.5f, regY - 0.5f, regZ + 0.5f), Vector3DFloat(-1.0f, 0.0f, 0.0f), material));
						uint32_t v2 = m_meshCurrent->addVertex(PositionMaterialNormal(Vector3DFloat(regX + 0.5f, regY + 0.5f, regZ - 0.5f), Vector3DFloat(-1.0f, 0.0f, 0.0f), material));
						uint32_t v3 = m_meshCurrent->addVertex(PositionMaterialNormal(Vector3DFloat(regX + 0.5f, regY + 0.5f, regZ + 0.5f), Vector3DFloat(-1.0f, 0.0f, 0.0f), material));

						m_meshCurrent->addTriangleCubic(v0,v1,v2);
						m_meshCurrent->addTriangleCubic(v1,v3,v2);
					}

					if(m_funcIsQuadNeededCallback(m_volData->getVoxelAt(x,y,z), m_volData->getVoxelAt(x,y+1,z), material))
					{
						uint32_t v0 = m_meshCurrent->addVertex(PositionMaterialNormal(Vector3DFloat(regX - 0.5f, regY + 0.5f, regZ - 0.5f), Vector3DFloat(0.0f, 1.0f, 0.0f), material));
						uint32_t v1 = m_meshCurrent->addVertex(PositionMaterialNormal(Vector3DFloat(regX - 0.5f, regY + 0.5f, regZ + 0.5f), Vector3DFloat(0.0f, 1.0f, 0.0f), material));
						uint32_t v2 = m_meshCurrent->addVertex(PositionMaterialNormal(Vector3DFloat(regX + 0.5f, regY + 0.5f, regZ - 0.5f), Vector3DFloat(0.0f, 1.0f, 0.0f), material));
						uint32_t v3 = m_meshCurrent->addVertex(PositionMaterialNormal(Vector3DFloat(regX + 0.5f, regY + 0.5f, regZ + 0.5f), Vector3DFloat(0.0f, 1.0f, 0.0f), material));

						m_meshCurrent->addTriangleCubic(v0,v1,v2);
						m_meshCurrent->addTriangleCubic(v1,v3,v2);
					}
					if(m_funcIsQuadNeededCallback(m_volData->getVoxelAt(x,y+1,z), m_volData->getVoxelAt(x,y,z), material))
					{
						uint32_t v0 = m_meshCurrent->addVertex(PositionMaterialNormal(Vector3DFloat(regX - 0.5f, regY + 0.5f, regZ - 0.5f), Vector3DFloat(0.0f, -1.0f, 0.0f), material));
						uint32_t v1 = m_meshCurrent->addVertex(PositionMaterialNormal(Vector3DFloat(regX - 0.5f, regY + 0.5f, regZ + 0.5f), Vector3DFloat(0.0f, -1.0f, 0.0f), material));
						uint32_t v2 = m_meshCurrent->addVertex(PositionMaterialNormal(Vector3DFloat(regX + 0.5f, regY + 0.5f, regZ - 0.5f), Vector3DFloat(0.0f, -1.0f, 0.0f), material));
						uint32_t v3 = m_meshCurrent->addVertex(PositionMaterialNormal(Vector3DFloat(regX + 0.5f, regY + 0.5f, regZ + 0.5f), Vector3DFloat(0.0f, -1.0f, 0.0f), material));

						m_meshCurrent->addTriangleCubic(v0,v2,v1);
						m_meshCurrent->addTriangleCubic(v1,v2,v3);
					}

					if(m_funcIsQuadNeededCallback(m_volData->getVoxelAt(x,y,z), m_volData->getVoxelAt(x,y,z+1), material))
					{
						uint32_t v0 = m_meshCurrent->addVertex(PositionMaterialNormal(Vector3DFloat(regX - 0.5f, regY - 0.5f, regZ + 0.5f), Vector3DFloat(0.0f, 0.0f, 1.0f), material));
						uint32_t v1 = m_meshCurrent->addVertex(PositionMaterialNormal(Vector3DFloat(regX - 0.5f, regY + 0.5f, regZ + 0.5f), Vector3DFloat(0.0f, 0.0f, 1.0f), material));
						uint32_t v2 = m_meshCurrent->addVertex(PositionMaterialNormal(Vector3DFloat(regX + 0.5f, regY - 0.5f, regZ + 0.5f), Vector3DFloat(0.0f, 0.0f, 1.0f), material));
						uint32_t v3 = m_meshCurrent->addVertex(PositionMaterialNormal(Vector3DFloat(regX + 0.5f, regY + 0.5f, regZ + 0.5f), Vector3DFloat(0.0f, 0.0f, 1.0f), material));

						m_meshCurrent->addTriangleCubic(v0,v2,v1);
						m_meshCurrent->addTriangleCubic(v1,v2,v3);
					}
					if(m_funcIsQuadNeededCallback(m_volData->getVoxelAt(x,y,z+1), m_volData->getVoxelAt(x,y,z), material))
					{
						uint32_t v0 = m_meshCurrent->addVertex(PositionMaterialNormal(Vector3DFloat(regX - 0.5f, regY - 0.5f, regZ + 0.5f), Vector3DFloat(0.0f, 0.0f, -1.0f), material));
						uint32_t v1 = m_meshCurrent->addVertex(PositionMaterialNormal(Vector3DFloat(regX - 0.5f, regY + 0.5f, regZ + 0.5f), Vector3DFloat(0.0f, 0.0f, -1.0f), material));
						uint32_t v2 = m_meshCurrent->addVertex(PositionMaterialNormal(Vector3DFloat(regX + 0.5f, regY - 0.5f, regZ + 0.5f), Vector3DFloat(0.0f, 0.0f, -1.0f), material));
						uint32_t v3 = m_meshCurrent->addVertex(PositionMaterialNormal(Vector3DFloat(regX + 0.5f, regY + 0.5f, regZ + 0.5f), Vector3DFloat(0.0f, 0.0f, -1.0f), material));

						m_meshCurrent->addTriangleCubic(v0,v1,v2);
						m_meshCurrent->addTriangleCubic(v1,v3,v2);
					}
				}
			}
		}

		m_meshCurrent->m_Region = m_regSizeInVoxels;

		m_meshCurrent->m_vecLodRecords.clear();
		LodRecord lodRecord;
		lodRecord.beginIndex = 0;
		lodRecord.endIndex = m_meshCurrent->getNoOfIndices();
		m_meshCurrent->m_vecLodRecords.push_back(lodRecord);
	}
}
