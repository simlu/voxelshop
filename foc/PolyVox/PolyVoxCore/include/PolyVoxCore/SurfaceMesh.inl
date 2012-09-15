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
	template <typename VertexType>
	SurfaceMesh<VertexType>::SurfaceMesh()
	{
		m_iTimeStamp = -1;
	}

	template <typename VertexType>
	SurfaceMesh<VertexType>::~SurfaceMesh()	  
	{
	}

	template <typename VertexType>
	const std::vector<uint32_t>& SurfaceMesh<VertexType>::getIndices(void) const
	{
		return m_vecTriangleIndices;
	}

	template <typename VertexType>
	uint32_t SurfaceMesh<VertexType>::getNoOfIndices(void) const
	{
		return m_vecTriangleIndices.size();
	}	

	template <typename VertexType>
	uint32_t SurfaceMesh<VertexType>::getNoOfNonUniformTrianges(void) const
	{
		uint32_t result = 0;
		for(uint32_t i = 0; i < m_vecTriangleIndices.size() - 2; i += 3)
		{
			if((m_vecVertices[m_vecTriangleIndices[i]].getMaterial() == m_vecVertices[m_vecTriangleIndices[i+1]].getMaterial())
			&& (m_vecVertices[m_vecTriangleIndices[i]].getMaterial() == m_vecVertices[m_vecTriangleIndices[i+2]].getMaterial()))
			{
			}
			else
			{
				result++;
			}
		}
		return result;
	}

	template <typename VertexType>
	uint32_t SurfaceMesh<VertexType>::getNoOfUniformTrianges(void) const
	{
		uint32_t result = 0;
		for(uint32_t i = 0; i < m_vecTriangleIndices.size() - 2; i += 3)
		{
			if((m_vecVertices[m_vecTriangleIndices[i]].getMaterial() == m_vecVertices[m_vecTriangleIndices[i+1]].getMaterial())
			&& (m_vecVertices[m_vecTriangleIndices[i]].getMaterial() == m_vecVertices[m_vecTriangleIndices[i+2]].getMaterial()))
			{
				result++;
			}
		}
		return result;
	}

	template <typename VertexType>
	uint32_t SurfaceMesh<VertexType>::getNoOfVertices(void) const
	{
		return m_vecVertices.size();
	}

	template <typename VertexType>
	std::vector<VertexType>& SurfaceMesh<VertexType>::getRawVertexData(void)
	{
		return m_vecVertices;
	}

	template <typename VertexType>
	const std::vector<VertexType>& SurfaceMesh<VertexType>::getVertices(void) const
	{
		return m_vecVertices;
	}		

	template <typename VertexType>
	void SurfaceMesh<VertexType>::addTriangle(uint32_t index0, uint32_t index1, uint32_t index2)
	{
		//Make sure the specified indices correspond to valid vertices.
		assert(index0 < m_vecVertices.size());
		assert(index1 < m_vecVertices.size());
		assert(index2 < m_vecVertices.size());

		m_vecTriangleIndices.push_back(index0);
		m_vecTriangleIndices.push_back(index1);
		m_vecTriangleIndices.push_back(index2);
	}

	template <typename VertexType>
	void SurfaceMesh<VertexType>::addTriangleCubic(uint32_t index0, uint32_t index1, uint32_t index2)
	{
		//Make sure the specified indices correspond to valid vertices.
		assert(index0 < m_vecVertices.size());
		assert(index1 < m_vecVertices.size());
		assert(index2 < m_vecVertices.size());

		m_vecTriangleIndices.push_back(index0);
		m_vecTriangleIndices.push_back(index1);
		m_vecTriangleIndices.push_back(index2);
	}

	template <typename VertexType>
	uint32_t SurfaceMesh<VertexType>::addVertex(const VertexType& vertex)
	{
		m_vecVertices.push_back(vertex);
		return m_vecVertices.size() - 1;
	}

	template <typename VertexType>
	void SurfaceMesh<VertexType>::clear(void)
	{
		m_vecVertices.clear();
		m_vecTriangleIndices.clear();
		m_vecLodRecords.clear();
	}

	template <typename VertexType>
	bool SurfaceMesh<VertexType>::isEmpty(void) const
	{
		return (getNoOfVertices() == 0) || (getNoOfIndices() == 0);
	}

	////////////////////////////////////////////////////////////////////////////////
	/// This function can help improve the visual appearance of a surface patch by
	/// smoothing normals with other nearby normals. It iterates over each triangle
	/// in the surface patch and determines the sum of its corners normals. For any
	/// given vertex, these sums are in turn summed for any triangles which use the
	/// vertex. Usually, the resulting normals should be renormalised afterwards.
	/// Note: This function can cause lighting discontinuities accross region boundaries.
	////////////////////////////////////////////////////////////////////////////////
	/*template <typename VertexType>
	void SurfaceMesh<VertexType>::sumNearbyNormals(bool bNormaliseResult)
	{
		if(m_vecVertices.size() == 0) //FIXME - I don't think we should need this test, but I have seen crashes otherwise...
		{
			return;
		}

		std::vector<Vector3DFloat> summedNormals(m_vecVertices.size());

		//Initialise all normals to zero. Should be ok as the vector should store all elements contiguously.
		memset(&summedNormals[0], 0, summedNormals.size() * sizeof(Vector3DFloat));

		for(vector<uint32_t>::iterator iterIndex = m_vecTriangleIndices.begin(); iterIndex != m_vecTriangleIndices.end();)
		{
			PositionMaterialNormal& v0 = m_vecVertices[*iterIndex];
			Vector3DFloat& v0New = summedNormals[*iterIndex];
			iterIndex++;
			PositionMaterialNormal& v1 = m_vecVertices[*iterIndex];
			Vector3DFloat& v1New = summedNormals[*iterIndex];
			iterIndex++;
			PositionMaterialNormal& v2 = m_vecVertices[*iterIndex];
			Vector3DFloat& v2New = summedNormals[*iterIndex];
			iterIndex++;

			Vector3DFloat sumOfNormals = v0.getNormal() + v1.getNormal() + v2.getNormal();

			v0New += sumOfNormals;
			v1New += sumOfNormals;
			v2New += sumOfNormals;
		}

		for(uint32_t uIndex = 0; uIndex < summedNormals.size(); uIndex++)
		{
			if(bNormaliseResult)
			{
				summedNormals[uIndex].normalise();
			}
			m_vecVertices[uIndex].setNormal(summedNormals[uIndex]);
		}
	}*/

	/*template <typename VertexType>
	void SurfaceMesh<VertexType>::generateAveragedFaceNormals(bool bNormalise, bool bIncludeEdgeVertices)
	{
		Vector3DFloat offset = static_cast<Vector3DFloat>(m_Region.getLowerCorner());

		//Initially zero the normals
		for(vector<PositionMaterialNormal>::iterator iterVertex = m_vecVertices.begin(); iterVertex != m_vecVertices.end(); iterVertex++)
		{
			if(m_Region.containsPoint(iterVertex->getPosition() + offset, 0.001))
			{
				iterVertex->setNormal(Vector3DFloat(0.0f,0.0f,0.0f));
			}
		}

		for(vector<uint32_t>::iterator iterIndex = m_vecTriangleIndices.begin(); iterIndex != m_vecTriangleIndices.end();)
		{
			PositionMaterialNormal& v0 = m_vecVertices[*iterIndex];
			iterIndex++;
			PositionMaterialNormal& v1 = m_vecVertices[*iterIndex];
			iterIndex++;
			PositionMaterialNormal& v2 = m_vecVertices[*iterIndex];
			iterIndex++;

			Vector3DFloat triangleNormal = (v1.getPosition()-v0.getPosition()).cross(v2.getPosition()-v0.getPosition());

			if(m_Region.containsPoint(v0.getPosition() + offset, 0.001))
			{
				v0.setNormal(v0.getNormal() + triangleNormal);
			}
			if(m_Region.containsPoint(v1.getPosition() + offset, 0.001))
			{
				v1.setNormal(v1.getNormal() + triangleNormal);
			}
			if(m_Region.containsPoint(v2.getPosition() + offset, 0.001))
			{
				v2.setNormal(v2.getNormal() + triangleNormal);
			}
		}

		if(bNormalise)
		{
			for(vector<PositionMaterialNormal>::iterator iterVertex = m_vecVertices.begin(); iterVertex != m_vecVertices.end(); iterVertex++)
			{
				Vector3DFloat normal = iterVertex->getNormal();
				normal.normalise();
				iterVertex->setNormal(normal);
			}
		}
	}*/

	/*template <typename VertexType>
	polyvox_shared_ptr< SurfaceMesh<VertexType> > SurfaceMesh<VertexType>::extractSubset(std::set<uint8_t> setMaterials)
	{
		polyvox_shared_ptr< SurfaceMesh<VertexType> > result(new SurfaceMesh<VertexType>);

		if(m_vecVertices.size() == 0) //FIXME - I don't think we should need this test, but I have seen crashes otherwise...
		{
			return result;
		}

		assert(m_vecLodRecords.size() == 1);
		if(m_vecLodRecords.size() != 1)
		{
			//If we have done progressive LOD then it's too late to split into subsets.
			return result;
		}

		std::vector<int32_t> indexMap(m_vecVertices.size());
		std::fill(indexMap.begin(), indexMap.end(), -1);

		for(uint32_t triCt = 0; triCt < m_vecTriangleIndices.size(); triCt += 3)
		{

			PositionMaterialNormal& v0 = m_vecVertices[m_vecTriangleIndices[triCt]];
			PositionMaterialNormal& v1 = m_vecVertices[m_vecTriangleIndices[triCt + 1]];
			PositionMaterialNormal& v2 = m_vecVertices[m_vecTriangleIndices[triCt + 2]];

			if(
				(setMaterials.find(v0.getMaterial()) != setMaterials.end()) || 
				(setMaterials.find(v1.getMaterial()) != setMaterials.end()) || 
				(setMaterials.find(v2.getMaterial()) != setMaterials.end()))
			{
				uint32_t i0;
				if(indexMap[m_vecTriangleIndices[triCt]] == -1)
				{
					indexMap[m_vecTriangleIndices[triCt]] = result->addVertex(v0);
				}
				i0 = indexMap[m_vecTriangleIndices[triCt]];

				uint32_t i1;
				if(indexMap[m_vecTriangleIndices[triCt+1]] == -1)
				{
					indexMap[m_vecTriangleIndices[triCt+1]] = result->addVertex(v1);
				}
				i1 = indexMap[m_vecTriangleIndices[triCt+1]];

				uint32_t i2;
				if(indexMap[m_vecTriangleIndices[triCt+2]] == -1)
				{
					indexMap[m_vecTriangleIndices[triCt+2]] = result->addVertex(v2);
				}
				i2 = indexMap[m_vecTriangleIndices[triCt+2]];

				result->addTriangle(i0,i1,i2);
			}
		}

		result->m_vecLodRecords.clear();
		LodRecord lodRecord;
		lodRecord.beginIndex = 0;
		lodRecord.endIndex = result->getNoOfIndices();
		result->m_vecLodRecords.push_back(lodRecord);

		return result;
	}*/

	template <typename VertexType>
	int SurfaceMesh<VertexType>::noOfDegenerateTris(void)
	{
		int count = 0;
		for(uint32_t triCt = 0; triCt < m_vecTriangleIndices.size();)
		{
			int v0 = m_vecTriangleIndices[triCt];
			triCt++;
			int v1 = m_vecTriangleIndices[triCt];
			triCt++;
			int v2 = m_vecTriangleIndices[triCt];
			triCt++;

			if((v0 == v1) || (v1 == v2) || (v2 == v0))
			{
				count++;
			}
		}
		return count;
	}

	template <typename VertexType>
	void SurfaceMesh<VertexType>::removeDegenerateTris(void)
	{
		int noOfNonDegenerate = 0;
		int targetCt = 0;
		for(uint32_t triCt = 0; triCt < m_vecTriangleIndices.size();)
		{
			int v0 = m_vecTriangleIndices[triCt];
			triCt++;
			int v1 = m_vecTriangleIndices[triCt];
			triCt++;
			int v2 = m_vecTriangleIndices[triCt];
			triCt++;

			if((v0 != v1) && (v1 != v2) & (v2 != v0))
			{
				m_vecTriangleIndices[targetCt] = v0;
				targetCt++;
				m_vecTriangleIndices[targetCt] = v1;
				targetCt++;
				m_vecTriangleIndices[targetCt] = v2;
				targetCt++;

				noOfNonDegenerate++;
			}
		}

		m_vecTriangleIndices.resize(noOfNonDegenerate * 3);
	}

	template <typename VertexType>
	void SurfaceMesh<VertexType>::removeUnusedVertices(void)
	{
		std::vector<bool> isVertexUsed(m_vecVertices.size());
		fill(isVertexUsed.begin(), isVertexUsed.end(), false);

		for(uint32_t triCt = 0; triCt < m_vecTriangleIndices.size(); triCt++)
		{
			int v = m_vecTriangleIndices[triCt];
			isVertexUsed[v] = true;
		}

		int noOfUsedVertices = 0;
		std::vector<uint32_t> newPos(m_vecVertices.size());
		for(uint32_t vertCt = 0; vertCt < m_vecVertices.size(); vertCt++)
		{
			if(isVertexUsed[vertCt])
			{
				m_vecVertices[noOfUsedVertices] = m_vecVertices[vertCt];
				newPos[vertCt] = noOfUsedVertices;
				noOfUsedVertices++;
			}
		}

		m_vecVertices.resize(noOfUsedVertices);

		for(uint32_t triCt = 0; triCt < m_vecTriangleIndices.size(); triCt++)
		{
			m_vecTriangleIndices[triCt] = newPos[m_vecTriangleIndices[triCt]];
		}
	}

	//Currently a free function - think where this needs to go.
	template <typename VertexType>
	polyvox_shared_ptr< SurfaceMesh<VertexType> > extractSubset(SurfaceMesh<VertexType>& inputMesh, std::set<uint8_t> setMaterials)
	{
		polyvox_shared_ptr< SurfaceMesh<VertexType> > result(new SurfaceMesh<VertexType>);
		
		result->m_Region = inputMesh.m_Region;

		if(inputMesh.m_vecVertices.size() == 0) //FIXME - I don't think we should need this test, but I have seen crashes otherwise...
		{
			return result;
		}

		assert(inputMesh.m_vecLodRecords.size() == 1);
		if(inputMesh.m_vecLodRecords.size() != 1)
		{
			//If we have done progressive LOD then it's too late to split into subsets.
			return result;
		}

		std::vector<int32_t> indexMap(inputMesh.m_vecVertices.size());
		std::fill(indexMap.begin(), indexMap.end(), -1);

		for(uint32_t triCt = 0; triCt < inputMesh.m_vecTriangleIndices.size(); triCt += 3)
		{

			VertexType& v0 = inputMesh.m_vecVertices[inputMesh.m_vecTriangleIndices[triCt]];
			VertexType& v1 = inputMesh.m_vecVertices[inputMesh.m_vecTriangleIndices[triCt + 1]];
			VertexType& v2 = inputMesh.m_vecVertices[inputMesh.m_vecTriangleIndices[triCt + 2]];

			if(
				(setMaterials.find(v0.getMaterial()) != setMaterials.end()) || 
				(setMaterials.find(v1.getMaterial()) != setMaterials.end()) || 
				(setMaterials.find(v2.getMaterial()) != setMaterials.end()))
			{
				uint32_t i0;
				if(indexMap[inputMesh.m_vecTriangleIndices[triCt]] == -1)
				{
					indexMap[inputMesh.m_vecTriangleIndices[triCt]] = result->addVertex(v0);
				}
				i0 = indexMap[inputMesh.m_vecTriangleIndices[triCt]];

				uint32_t i1;
				if(indexMap[inputMesh.m_vecTriangleIndices[triCt+1]] == -1)
				{
					indexMap[inputMesh.m_vecTriangleIndices[triCt+1]] = result->addVertex(v1);
				}
				i1 = indexMap[inputMesh.m_vecTriangleIndices[triCt+1]];

				uint32_t i2;
				if(indexMap[inputMesh.m_vecTriangleIndices[triCt+2]] == -1)
				{
					indexMap[inputMesh.m_vecTriangleIndices[triCt+2]] = result->addVertex(v2);
				}
				i2 = indexMap[inputMesh.m_vecTriangleIndices[triCt+2]];

				result->addTriangle(i0,i1,i2);
			}
		}

		result->m_vecLodRecords.clear();
		LodRecord lodRecord;
		lodRecord.beginIndex = 0;
		lodRecord.endIndex = result->getNoOfIndices();
		result->m_vecLodRecords.push_back(lodRecord);

		return result;
	}

	template <typename VertexType>
	void SurfaceMesh<VertexType>::scaleVertices(float amount)
	{
		for(uint32_t ct = 0; ct < m_vecVertices.size(); ct++)
		{
			//TODO: Should rethink accessors here to provide faster access
			Vector3DFloat position = m_vecVertices[ct].getPosition();
			position *= amount;
			m_vecVertices[ct].setPosition(position);
		}
	}

	template <typename VertexType>
	void SurfaceMesh<VertexType>::translateVertices(const Vector3DFloat& amount)
	{
		for(uint32_t ct = 0; ct < m_vecVertices.size(); ct++)
		{
			//TODO: Should rethink accessors here to provide faster access
			Vector3DFloat position = m_vecVertices[ct].getPosition();
			position += amount;
			m_vecVertices[ct].setPosition(position);
		}
	}
}
