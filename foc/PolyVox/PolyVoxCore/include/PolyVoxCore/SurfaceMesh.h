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

#ifndef __PolyVox_SurfaceMesh_H__
#define __PolyVox_SurfaceMesh_H__

#include "PolyVoxImpl/TypeDef.h"

#include "PolyVoxCore/Region.h"
#include "PolyVoxCore/VertexTypes.h" //Should probably do away with this on in the future...

#include <algorithm>
#include <cstdlib>
#include <list>
#include <memory>
#include <set>
#include <vector>

namespace PolyVox
{
	class LodRecord
	{
	public:
		int beginIndex;
		int endIndex; //Let's put it just past the end STL style
	};

	template <typename VertexType>
	class SurfaceMesh
	{
	public:
	   SurfaceMesh();
	   ~SurfaceMesh();	   

	   const std::vector<uint32_t>& getIndices(void) const;
	   uint32_t getNoOfIndices(void) const;
	   uint32_t getNoOfNonUniformTrianges(void) const;
	   uint32_t getNoOfUniformTrianges(void) const;
	   uint32_t getNoOfVertices(void) const;	   
	   std::vector<VertexType>& getRawVertexData(void); //FIXME - this should be removed
	   const std::vector<VertexType>& getVertices(void) const;

	   void addTriangle(uint32_t index0, uint32_t index1, uint32_t index2);
	   void addTriangleCubic(uint32_t index0, uint32_t index1, uint32_t index2);
	   uint32_t addVertex(const VertexType& vertex);
	   void clear(void);
	   bool isEmpty(void) const;

	   void scaleVertices(float amount);
	   void translateVertices(const Vector3DFloat& amount);

	   //THESE FUNCTIONS TO BE REMOVED IN THE FUTURE. OR AT LEAST MOVED OUT OF THIS CLASS INTO FREE FUNCTIONS.
	   //THEY ARE CAUSING PROBLEMS WITH THE SWIG BINDINGS. THE FUNCTIONS REGARDING NORMALS MAKE NO SENSE WHEN
	   //A VERTEX MIGHT NOT HAVE NORMALS. THE EXTRACT SUBSET FUNCTION SHOULD MAYBE BE APPLICATION CODE, AT ANY
	   //RATE THE STD::SET CAUSES PROBLEMS WITH SWIG. IF YOU UNCOMMENT ANY OF THESE FUNCTIONS, PLEASE POST ON
	   //THE FORUM SO WE CAN KNOW THE FUNCTIONALITY IS STILL NEEDED IN SOME FORM.
	   //void sumNearbyNormals(bool bNormaliseResult = true);
	   //polyvox_shared_ptr< SurfaceMesh<VertexType> > extractSubset(std::set<uint8_t> setMaterials);
	   //void generateAveragedFaceNormals(bool bNormalise, bool bIncludeEdgeVertices = false);

	   int noOfDegenerateTris(void);
	   void removeDegenerateTris(void);
	   void removeUnusedVertices(void);

	   Region m_Region;

	   int32_t m_iTimeStamp;

	   int32_t m_iNoOfLod0Tris;
	
	public:		
		std::vector<uint32_t> m_vecTriangleIndices;
		std::vector<VertexType> m_vecVertices;

		std::vector<LodRecord> m_vecLodRecords;
	};	

	template <typename VertexType>
	polyvox_shared_ptr< SurfaceMesh<VertexType> > extractSubset(SurfaceMesh<VertexType>& inputMesh, std::set<uint8_t> setMaterials);
}

#include "PolyVoxCore/SurfaceMesh.inl"

#endif /* __SurfaceMesh_H__ */
