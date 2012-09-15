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
	template<typename VolumeType, typename Controller>
	MarchingCubesSurfaceExtractor<VolumeType, Controller>::MarchingCubesSurfaceExtractor(VolumeType* volData, Region region, SurfaceMesh<PositionMaterialNormal>* result, Controller controller)
		:m_volData(volData)
		,m_sampVolume(volData)
		,m_meshCurrent(result)
		,m_regSizeInVoxels(region)
	{
		//m_regSizeInVoxels.cropTo(m_volData->getEnclosingRegion());
		m_regSizeInCells = m_regSizeInVoxels;
		m_regSizeInCells.setUpperCorner(m_regSizeInCells.getUpperCorner() - Vector3DInt32(1,1,1));

		m_controller = controller;
		m_tThreshold = m_controller.getThreshold();
	}

	template<typename VolumeType, typename Controller>
	void MarchingCubesSurfaceExtractor<VolumeType, Controller>::execute()
	{		
		m_meshCurrent->clear();

		uint32_t uArrayWidth = m_regSizeInVoxels.getUpperCorner().getX() - m_regSizeInVoxels.getLowerCorner().getX() + 1;
		uint32_t uArrayHeight = m_regSizeInVoxels.getUpperCorner().getY() - m_regSizeInVoxels.getLowerCorner().getY() + 1;
		uint32_t arraySizes[2]= {uArrayWidth, uArrayHeight}; // Array dimensions

		//For edge indices
		Array2DInt32 m_pPreviousVertexIndicesX(arraySizes);
		Array2DInt32 m_pPreviousVertexIndicesY(arraySizes);
		Array2DInt32 m_pPreviousVertexIndicesZ(arraySizes);
		Array2DInt32 m_pCurrentVertexIndicesX(arraySizes);
		Array2DInt32 m_pCurrentVertexIndicesY(arraySizes);
		Array2DInt32 m_pCurrentVertexIndicesZ(arraySizes);

		Array2DUint8 pPreviousBitmask(arraySizes);
		Array2DUint8 pCurrentBitmask(arraySizes);

		//Create a region corresponding to the first slice
		m_regSlicePrevious = m_regSizeInVoxels;
		Vector3DInt32 v3dUpperCorner = m_regSlicePrevious.getUpperCorner();
		v3dUpperCorner.setZ(m_regSlicePrevious.getLowerCorner().getZ()); //Set the upper z to the lower z to make it one slice thick.
		m_regSlicePrevious.setUpperCorner(v3dUpperCorner);
		m_regSliceCurrent = m_regSlicePrevious;	

		uint32_t uNoOfNonEmptyCellsForSlice0 = 0;
		uint32_t uNoOfNonEmptyCellsForSlice1 = 0;

		//Process the first slice (previous slice not available)
		computeBitmaskForSlice<false>(pPreviousBitmask, pCurrentBitmask);
		uNoOfNonEmptyCellsForSlice1 = m_uNoOfOccupiedCells;

		if(uNoOfNonEmptyCellsForSlice1 != 0)
		{
			memset(m_pCurrentVertexIndicesX.getRawData(), 0xff, m_pCurrentVertexIndicesX.getNoOfElements() * 4);
			memset(m_pCurrentVertexIndicesY.getRawData(), 0xff, m_pCurrentVertexIndicesY.getNoOfElements() * 4);
			memset(m_pCurrentVertexIndicesZ.getRawData(), 0xff, m_pCurrentVertexIndicesZ.getNoOfElements() * 4);
			generateVerticesForSlice(pCurrentBitmask, m_pCurrentVertexIndicesX, m_pCurrentVertexIndicesY, m_pCurrentVertexIndicesZ);				
		}

		std::swap(uNoOfNonEmptyCellsForSlice0, uNoOfNonEmptyCellsForSlice1);
		pPreviousBitmask.swap(pCurrentBitmask);
		m_pPreviousVertexIndicesX.swap(m_pCurrentVertexIndicesX);
		m_pPreviousVertexIndicesY.swap(m_pCurrentVertexIndicesY);
		m_pPreviousVertexIndicesZ.swap(m_pCurrentVertexIndicesZ);

		m_regSlicePrevious = m_regSliceCurrent;
		m_regSliceCurrent.shift(Vector3DInt32(0,0,1));

		//Process the other slices (previous slice is available)
		for(int32_t uSlice = 1; uSlice <= m_regSizeInVoxels.getUpperCorner().getZ() - m_regSizeInVoxels.getLowerCorner().getZ(); uSlice++)
		{	
			computeBitmaskForSlice<true>(pPreviousBitmask, pCurrentBitmask);
			uNoOfNonEmptyCellsForSlice1 = m_uNoOfOccupiedCells;

			if(uNoOfNonEmptyCellsForSlice1 != 0)
			{
				memset(m_pCurrentVertexIndicesX.getRawData(), 0xff, m_pCurrentVertexIndicesX.getNoOfElements() * 4);
				memset(m_pCurrentVertexIndicesY.getRawData(), 0xff, m_pCurrentVertexIndicesY.getNoOfElements() * 4);
				memset(m_pCurrentVertexIndicesZ.getRawData(), 0xff, m_pCurrentVertexIndicesZ.getNoOfElements() * 4);
				generateVerticesForSlice(pCurrentBitmask, m_pCurrentVertexIndicesX, m_pCurrentVertexIndicesY, m_pCurrentVertexIndicesZ);				
			}

			if((uNoOfNonEmptyCellsForSlice0 != 0) || (uNoOfNonEmptyCellsForSlice1 != 0))
			{
				generateIndicesForSlice(pPreviousBitmask, m_pPreviousVertexIndicesX, m_pPreviousVertexIndicesY, m_pPreviousVertexIndicesZ, m_pCurrentVertexIndicesX, m_pCurrentVertexIndicesY);
			}

			std::swap(uNoOfNonEmptyCellsForSlice0, uNoOfNonEmptyCellsForSlice1);
			pPreviousBitmask.swap(pCurrentBitmask);
			m_pPreviousVertexIndicesX.swap(m_pCurrentVertexIndicesX);
			m_pPreviousVertexIndicesY.swap(m_pCurrentVertexIndicesY);
			m_pPreviousVertexIndicesZ.swap(m_pCurrentVertexIndicesZ);

			m_regSlicePrevious = m_regSliceCurrent;
			m_regSliceCurrent.shift(Vector3DInt32(0,0,1));
		}

		m_meshCurrent->m_Region = m_regSizeInVoxels;

		m_meshCurrent->m_vecLodRecords.clear();
		LodRecord lodRecord;
		lodRecord.beginIndex = 0;
		lodRecord.endIndex = m_meshCurrent->getNoOfIndices();
		m_meshCurrent->m_vecLodRecords.push_back(lodRecord);
	}

	template<typename VolumeType, typename Controller>
	template<bool isPrevZAvail>
	uint32_t MarchingCubesSurfaceExtractor<VolumeType, Controller>::computeBitmaskForSlice(const Array2DUint8& pPreviousBitmask, Array2DUint8& pCurrentBitmask)
	{
		m_uNoOfOccupiedCells = 0;

		const int32_t iMaxXVolSpace = m_regSliceCurrent.getUpperCorner().getX();
		const int32_t iMaxYVolSpace = m_regSliceCurrent.getUpperCorner().getY();

		iZVolSpace = m_regSliceCurrent.getLowerCorner().getZ();
		uZRegSpace = iZVolSpace - m_regSizeInVoxels.getLowerCorner().getZ();

		//Process the lower left corner
		iYVolSpace = m_regSliceCurrent.getLowerCorner().getY();
		iXVolSpace = m_regSliceCurrent.getLowerCorner().getX();

		uXRegSpace = iXVolSpace - m_regSizeInVoxels.getLowerCorner().getX();
		uYRegSpace = iYVolSpace - m_regSizeInVoxels.getLowerCorner().getY();

		m_sampVolume.setPosition(iXVolSpace,iYVolSpace,iZVolSpace);
		computeBitmaskForCell<false, false, isPrevZAvail>(pPreviousBitmask, pCurrentBitmask);

		//Process the edge where x is minimal.
		iXVolSpace = m_regSliceCurrent.getLowerCorner().getX();
		m_sampVolume.setPosition(iXVolSpace, m_regSliceCurrent.getLowerCorner().getY(), iZVolSpace);
		for(iYVolSpace = m_regSliceCurrent.getLowerCorner().getY() + 1; iYVolSpace <= iMaxYVolSpace; iYVolSpace++)
		{
			uXRegSpace = iXVolSpace - m_regSizeInVoxels.getLowerCorner().getX();
			uYRegSpace = iYVolSpace - m_regSizeInVoxels.getLowerCorner().getY();

			m_sampVolume.movePositiveY();

			computeBitmaskForCell<false, true, isPrevZAvail>(pPreviousBitmask, pCurrentBitmask);
		}

		//Process the edge where y is minimal.
		iYVolSpace = m_regSliceCurrent.getLowerCorner().getY();
		m_sampVolume.setPosition(m_regSliceCurrent.getLowerCorner().getX(), iYVolSpace, iZVolSpace);
		for(iXVolSpace = m_regSliceCurrent.getLowerCorner().getX() + 1; iXVolSpace <= iMaxXVolSpace; iXVolSpace++)
		{	
			uXRegSpace = iXVolSpace - m_regSizeInVoxels.getLowerCorner().getX();
			uYRegSpace = iYVolSpace - m_regSizeInVoxels.getLowerCorner().getY();

			m_sampVolume.movePositiveX();

			computeBitmaskForCell<true, false, isPrevZAvail>(pPreviousBitmask, pCurrentBitmask);
		}

		//Process all remaining elemnents of the slice. In this case, previous x and y values are always available
		for(iYVolSpace = m_regSliceCurrent.getLowerCorner().getY() + 1; iYVolSpace <= iMaxYVolSpace; iYVolSpace++)
		{
			m_sampVolume.setPosition(m_regSliceCurrent.getLowerCorner().getX(), iYVolSpace, iZVolSpace);
			for(iXVolSpace = m_regSliceCurrent.getLowerCorner().getX() + 1; iXVolSpace <= iMaxXVolSpace; iXVolSpace++)
			{	
				uXRegSpace = iXVolSpace - m_regSizeInVoxels.getLowerCorner().getX();
				uYRegSpace = iYVolSpace - m_regSizeInVoxels.getLowerCorner().getY();

				m_sampVolume.movePositiveX();

				computeBitmaskForCell<true, true, isPrevZAvail>(pPreviousBitmask, pCurrentBitmask);
			}
		}

		return m_uNoOfOccupiedCells;
	}

	template<typename VolumeType, typename Controller>
	template<bool isPrevXAvail, bool isPrevYAvail, bool isPrevZAvail>
	void MarchingCubesSurfaceExtractor<VolumeType, Controller>::computeBitmaskForCell(const Array2DUint8& pPreviousBitmask, Array2DUint8& pCurrentBitmask)
	{
		uint8_t iCubeIndex = 0;

		typename VolumeType::VoxelType v000;
		typename VolumeType::VoxelType v100;
		typename VolumeType::VoxelType v010;
		typename VolumeType::VoxelType v110;
		typename VolumeType::VoxelType v001;
		typename VolumeType::VoxelType v101;
		typename VolumeType::VoxelType v011;
		typename VolumeType::VoxelType v111;

		if(isPrevZAvail)
		{
			if(isPrevYAvail)
			{
				if(isPrevXAvail)
				{
					v111 = m_sampVolume.peekVoxel1px1py1pz();

					//z
					uint8_t iPreviousCubeIndexZ = pPreviousBitmask[uXRegSpace][uYRegSpace];
					iPreviousCubeIndexZ >>= 4;

					//y
					uint8_t iPreviousCubeIndexY = pCurrentBitmask[uXRegSpace][uYRegSpace-1];
					iPreviousCubeIndexY &= 192; //192 = 128 + 64
					iPreviousCubeIndexY >>= 2;

					//x
					uint8_t iPreviousCubeIndexX = pCurrentBitmask[uXRegSpace-1][uYRegSpace];
					iPreviousCubeIndexX &= 128;
					iPreviousCubeIndexX >>= 1;

					iCubeIndex = iPreviousCubeIndexX | iPreviousCubeIndexY | iPreviousCubeIndexZ;

					if (m_controller.convertToDensity(v111) < m_tThreshold) iCubeIndex |= 128;
				}
				else //previous X not available
				{
					v011 = m_sampVolume.peekVoxel0px1py1pz();
					v111 = m_sampVolume.peekVoxel1px1py1pz();

					//z
					uint8_t iPreviousCubeIndexZ = pPreviousBitmask[uXRegSpace][uYRegSpace];
					iPreviousCubeIndexZ >>= 4;

					//y
					uint8_t iPreviousCubeIndexY = pCurrentBitmask[uXRegSpace][uYRegSpace-1];
					iPreviousCubeIndexY &= 192; //192 = 128 + 64
					iPreviousCubeIndexY >>= 2;

					iCubeIndex = iPreviousCubeIndexY | iPreviousCubeIndexZ;

					if (m_controller.convertToDensity(v011) < m_tThreshold) iCubeIndex |= 64;
					if (m_controller.convertToDensity(v111) < m_tThreshold) iCubeIndex |= 128;
				}
			}
			else //previous Y not available
			{
				if(isPrevXAvail)
				{
					v101 = m_sampVolume.peekVoxel1px0py1pz();
					v111 = m_sampVolume.peekVoxel1px1py1pz();

					//z
					uint8_t iPreviousCubeIndexZ = pPreviousBitmask[uXRegSpace][uYRegSpace];
					iPreviousCubeIndexZ >>= 4;

					//x
					uint8_t iPreviousCubeIndexX = pCurrentBitmask[uXRegSpace-1][uYRegSpace];
					iPreviousCubeIndexX &= 160; //160 = 128+32
					iPreviousCubeIndexX >>= 1;

					iCubeIndex = iPreviousCubeIndexX | iPreviousCubeIndexZ;

					if (m_controller.convertToDensity(v101) < m_tThreshold) iCubeIndex |= 32;
					if (m_controller.convertToDensity(v111) < m_tThreshold) iCubeIndex |= 128;
				}
				else //previous X not available
				{
					v001 = m_sampVolume.peekVoxel0px0py1pz();
					v101 = m_sampVolume.peekVoxel1px0py1pz();
					v011 = m_sampVolume.peekVoxel0px1py1pz();
					v111 = m_sampVolume.peekVoxel1px1py1pz();

					//z
					uint8_t iPreviousCubeIndexZ = pPreviousBitmask[uXRegSpace][uYRegSpace];
					iCubeIndex = iPreviousCubeIndexZ >> 4;

					if (m_controller.convertToDensity(v001) < m_tThreshold) iCubeIndex |= 16;
					if (m_controller.convertToDensity(v101) < m_tThreshold) iCubeIndex |= 32;
					if (m_controller.convertToDensity(v011) < m_tThreshold) iCubeIndex |= 64;
					if (m_controller.convertToDensity(v111) < m_tThreshold) iCubeIndex |= 128;
				}
			}
		}
		else //previous Z not available
		{
			if(isPrevYAvail)
			{
				if(isPrevXAvail)
				{
					v110 = m_sampVolume.peekVoxel1px1py0pz();
					v111 = m_sampVolume.peekVoxel1px1py1pz();

					//y
					uint8_t iPreviousCubeIndexY = pCurrentBitmask[uXRegSpace][uYRegSpace-1];
					iPreviousCubeIndexY &= 204; //204 = 128+64+8+4
					iPreviousCubeIndexY >>= 2;

					//x
					uint8_t iPreviousCubeIndexX = pCurrentBitmask[uXRegSpace-1][uYRegSpace];
					iPreviousCubeIndexX &= 170; //170 = 128+32+8+2
					iPreviousCubeIndexX >>= 1;

					iCubeIndex = iPreviousCubeIndexX | iPreviousCubeIndexY;

					if (m_controller.convertToDensity(v110) < m_tThreshold) iCubeIndex |= 8;
					if (m_controller.convertToDensity(v111) < m_tThreshold) iCubeIndex |= 128;
				}
				else //previous X not available
				{
					v010 = m_sampVolume.peekVoxel0px1py0pz();
					v110 = m_sampVolume.peekVoxel1px1py0pz();

					v011 = m_sampVolume.peekVoxel0px1py1pz();
					v111 = m_sampVolume.peekVoxel1px1py1pz();

					//y
					uint8_t iPreviousCubeIndexY = pCurrentBitmask[uXRegSpace][uYRegSpace-1];
					iPreviousCubeIndexY &= 204; //204 = 128+64+8+4
					iPreviousCubeIndexY >>= 2;

					iCubeIndex = iPreviousCubeIndexY;

					if (m_controller.convertToDensity(v010) < m_tThreshold) iCubeIndex |= 4;
					if (m_controller.convertToDensity(v110) < m_tThreshold) iCubeIndex |= 8;
					if (m_controller.convertToDensity(v011) < m_tThreshold) iCubeIndex |= 64;
					if (m_controller.convertToDensity(v111) < m_tThreshold) iCubeIndex |= 128;
				}
			}
			else //previous Y not available
			{
				if(isPrevXAvail)
				{
					v100 = m_sampVolume.peekVoxel1px0py0pz();
					v110 = m_sampVolume.peekVoxel1px1py0pz();

					v101 = m_sampVolume.peekVoxel1px0py1pz();
					v111 = m_sampVolume.peekVoxel1px1py1pz();

					//x
					uint8_t iPreviousCubeIndexX = pCurrentBitmask[uXRegSpace-1][uYRegSpace];
					iPreviousCubeIndexX &= 170; //170 = 128+32+8+2
					iPreviousCubeIndexX >>= 1;

					iCubeIndex = iPreviousCubeIndexX;

					if (m_controller.convertToDensity(v100) < m_tThreshold) iCubeIndex |= 2;	
					if (m_controller.convertToDensity(v110) < m_tThreshold) iCubeIndex |= 8;
					if (m_controller.convertToDensity(v101) < m_tThreshold) iCubeIndex |= 32;
					if (m_controller.convertToDensity(v111) < m_tThreshold) iCubeIndex |= 128;
				}
				else //previous X not available
				{
					v000 = m_sampVolume.getVoxel();
					v100 = m_sampVolume.peekVoxel1px0py0pz();
					v010 = m_sampVolume.peekVoxel0px1py0pz();
					v110 = m_sampVolume.peekVoxel1px1py0pz();

					v001 = m_sampVolume.peekVoxel0px0py1pz();
					v101 = m_sampVolume.peekVoxel1px0py1pz();
					v011 = m_sampVolume.peekVoxel0px1py1pz();
					v111 = m_sampVolume.peekVoxel1px1py1pz();

					if (m_controller.convertToDensity(v000) < m_tThreshold) iCubeIndex |= 1;
					if (m_controller.convertToDensity(v100) < m_tThreshold) iCubeIndex |= 2;
					if (m_controller.convertToDensity(v010) < m_tThreshold) iCubeIndex |= 4;
					if (m_controller.convertToDensity(v110) < m_tThreshold) iCubeIndex |= 8;
					if (m_controller.convertToDensity(v001) < m_tThreshold) iCubeIndex |= 16;
					if (m_controller.convertToDensity(v101) < m_tThreshold) iCubeIndex |= 32;
					if (m_controller.convertToDensity(v011) < m_tThreshold) iCubeIndex |= 64;
					if (m_controller.convertToDensity(v111) < m_tThreshold) iCubeIndex |= 128;
				}
			}
		}

		//Save the bitmask
		pCurrentBitmask[uXRegSpace][iYVolSpace- m_regSizeInVoxels.getLowerCorner().getY()] = iCubeIndex;

		if(edgeTable[iCubeIndex] != 0)
		{
			++m_uNoOfOccupiedCells;
		}
	}

	template<typename VolumeType, typename Controller>
	void MarchingCubesSurfaceExtractor<VolumeType, Controller>::generateVerticesForSlice(const Array2DUint8& pCurrentBitmask,
		Array2DInt32& m_pCurrentVertexIndicesX,
		Array2DInt32& m_pCurrentVertexIndicesY,
		Array2DInt32& m_pCurrentVertexIndicesZ)
	{
		int32_t iZVolSpace = m_regSliceCurrent.getLowerCorner().getZ();

		//Iterate over each cell in the region
		for(int32_t iYVolSpace = m_regSliceCurrent.getLowerCorner().getY(); iYVolSpace <= m_regSliceCurrent.getUpperCorner().getY(); iYVolSpace++)
		{
			const uint32_t uYRegSpace = iYVolSpace - m_regSizeInVoxels.getLowerCorner().getY();

			for(int32_t iXVolSpace = m_regSliceCurrent.getLowerCorner().getX(); iXVolSpace <= m_regSliceCurrent.getUpperCorner().getX(); iXVolSpace++)
			{		
				//Current position
				const uint32_t uXRegSpace = iXVolSpace - m_regSizeInVoxels.getLowerCorner().getX();

				//Determine the index into the edge table which tells us which vertices are inside of the surface
				uint8_t iCubeIndex = pCurrentBitmask[uXRegSpace][uYRegSpace];

				/* Cube is entirely in/out of the surface */
				if (edgeTable[iCubeIndex] == 0)
				{
					continue;
				}

				//Check whether the generated vertex will lie on the edge of the region


				m_sampVolume.setPosition(iXVolSpace,iYVolSpace,iZVolSpace);
				const typename VolumeType::VoxelType v000 = m_sampVolume.getVoxel();
				const Vector3DFloat n000 = computeCentralDifferenceGradient(m_sampVolume);

				/* Find the vertices where the surface intersects the cube */
				if (edgeTable[iCubeIndex] & 1)
				{
					m_sampVolume.movePositiveX();
					const typename VolumeType::VoxelType v100 = m_sampVolume.getVoxel();
					const Vector3DFloat n100 = computeCentralDifferenceGradient(m_sampVolume);

					float fInterp = static_cast<float>(m_tThreshold - m_controller.convertToDensity(v000)) / static_cast<float>(m_controller.convertToDensity(v100) - m_controller.convertToDensity(v000));

					const Vector3DFloat v3dPosition(static_cast<float>(iXVolSpace - m_regSizeInVoxels.getLowerCorner().getX()) + fInterp, static_cast<float>(iYVolSpace - m_regSizeInVoxels.getLowerCorner().getY()), static_cast<float>(iZVolSpace - m_regSizeInCells.getLowerCorner().getZ()));

					Vector3DFloat v3dNormal = (n100*fInterp) + (n000*(1-fInterp));
					v3dNormal.normalise();

					//Choose one of the two materials to use for the vertex (we don't interpolate as interpolation of
					//material IDs does not make sense). We take the largest, so that if we are working on a material-only
					//volume we get the one which is non-zero. Both materials can be non-zero if our volume has a density component.
					typename Controller::MaterialType uMaterial000 = m_controller.convertToMaterial(v000);
					typename Controller::MaterialType uMaterial100 = m_controller.convertToMaterial(v100);
					typename Controller::MaterialType uMaterial = (std::max)(uMaterial000, uMaterial100);

					PositionMaterialNormal surfaceVertex(v3dPosition, v3dNormal, static_cast<float>(uMaterial));
					uint32_t uLastVertexIndex = m_meshCurrent->addVertex(surfaceVertex);
					m_pCurrentVertexIndicesX[iXVolSpace - m_regSizeInVoxels.getLowerCorner().getX()][iYVolSpace - m_regSizeInVoxels.getLowerCorner().getY()] = uLastVertexIndex;

					m_sampVolume.moveNegativeX();
				}
				if (edgeTable[iCubeIndex] & 8)
				{
					m_sampVolume.movePositiveY();
					const typename VolumeType::VoxelType v010 = m_sampVolume.getVoxel();
					const Vector3DFloat n010 = computeCentralDifferenceGradient(m_sampVolume);

					float fInterp = static_cast<float>(m_tThreshold - m_controller.convertToDensity(v000)) / static_cast<float>(m_controller.convertToDensity(v010) - m_controller.convertToDensity(v000));

					const Vector3DFloat v3dPosition(static_cast<float>(iXVolSpace - m_regSizeInVoxels.getLowerCorner().getX()), static_cast<float>(iYVolSpace - m_regSizeInVoxels.getLowerCorner().getY()) + fInterp, static_cast<float>(iZVolSpace - m_regSizeInVoxels.getLowerCorner().getZ()));

					Vector3DFloat v3dNormal = (n010*fInterp) + (n000*(1-fInterp));
					v3dNormal.normalise();

					//Choose one of the two materials to use for the vertex (we don't interpolate as interpolation of
					//material IDs does not make sense). We take the largest, so that if we are working on a material-only
					//volume we get the one which is non-zero. Both materials can be non-zero if our volume has a density component.
					typename Controller::MaterialType uMaterial000 = m_controller.convertToMaterial(v000);
					typename Controller::MaterialType uMaterial010 = m_controller.convertToMaterial(v010);
					typename Controller::MaterialType uMaterial = (std::max)(uMaterial000, uMaterial010);

					PositionMaterialNormal surfaceVertex(v3dPosition, v3dNormal, static_cast<float>(uMaterial));
					uint32_t uLastVertexIndex = m_meshCurrent->addVertex(surfaceVertex);
					m_pCurrentVertexIndicesY[iXVolSpace - m_regSizeInVoxels.getLowerCorner().getX()][iYVolSpace - m_regSizeInVoxels.getLowerCorner().getY()] = uLastVertexIndex;

					m_sampVolume.moveNegativeY();
				}
				if (edgeTable[iCubeIndex] & 256)
				{
					m_sampVolume.movePositiveZ();
					const typename VolumeType::VoxelType v001 = m_sampVolume.getVoxel();
					const Vector3DFloat n001 = computeCentralDifferenceGradient(m_sampVolume);

					float fInterp = static_cast<float>(m_tThreshold - m_controller.convertToDensity(v000)) / static_cast<float>(m_controller.convertToDensity(v001) - m_controller.convertToDensity(v000));

					const Vector3DFloat v3dPosition(static_cast<float>(iXVolSpace - m_regSizeInVoxels.getLowerCorner().getX()), static_cast<float>(iYVolSpace - m_regSizeInVoxels.getLowerCorner().getY()), static_cast<float>(iZVolSpace - m_regSizeInVoxels.getLowerCorner().getZ()) + fInterp);

					Vector3DFloat v3dNormal = (n001*fInterp) + (n000*(1-fInterp));
					v3dNormal.normalise();

					//Choose one of the two materials to use for the vertex (we don't interpolate as interpolation of
					//material IDs does not make sense). We take the largest, so that if we are working on a material-only
					//volume we get the one which is non-zero. Both materials can be non-zero if our volume has a density component.
					typename Controller::MaterialType uMaterial000 = m_controller.convertToMaterial(v000);
					typename Controller::MaterialType uMaterial001 = m_controller.convertToMaterial(v001);
					typename Controller::MaterialType uMaterial = (std::max)(uMaterial000, uMaterial001);

					PositionMaterialNormal surfaceVertex(v3dPosition, v3dNormal, static_cast<float>(uMaterial));
					uint32_t uLastVertexIndex = m_meshCurrent->addVertex(surfaceVertex);
					m_pCurrentVertexIndicesZ[iXVolSpace - m_regSizeInVoxels.getLowerCorner().getX()][iYVolSpace - m_regSizeInVoxels.getLowerCorner().getY()] = uLastVertexIndex;

					m_sampVolume.moveNegativeZ();
				}
			}//For each cell
		}
	}

	template<typename VolumeType, typename Controller>
	void MarchingCubesSurfaceExtractor<VolumeType, Controller>::generateIndicesForSlice(const Array2DUint8& pPreviousBitmask,
		const Array2DInt32& m_pPreviousVertexIndicesX,
		const Array2DInt32& m_pPreviousVertexIndicesY,
		const Array2DInt32& m_pPreviousVertexIndicesZ,
		const Array2DInt32& m_pCurrentVertexIndicesX,
		const Array2DInt32& m_pCurrentVertexIndicesY)
	{
		int32_t indlist[12];
		for(int i = 0; i < 12; i++)
		{
			indlist[i] = -1;
		}

		for(int32_t iYVolSpace = m_regSlicePrevious.getLowerCorner().getY(); iYVolSpace <= m_regSizeInCells.getUpperCorner().getY(); iYVolSpace++)
		{
			for(int32_t iXVolSpace = m_regSlicePrevious.getLowerCorner().getX(); iXVolSpace <= m_regSizeInCells.getUpperCorner().getX(); iXVolSpace++)
			{		
				int32_t iZVolSpace = m_regSlicePrevious.getLowerCorner().getZ();
				m_sampVolume.setPosition(iXVolSpace,iYVolSpace,iZVolSpace);	

				//Current position
				const uint32_t uXRegSpace = m_sampVolume.getPosition().getX() - m_regSizeInVoxels.getLowerCorner().getX();
				const uint32_t uYRegSpace = m_sampVolume.getPosition().getY() - m_regSizeInVoxels.getLowerCorner().getY();

				//Determine the index into the edge table which tells us which vertices are inside of the surface
				uint8_t iCubeIndex = pPreviousBitmask[uXRegSpace][uYRegSpace];

				/* Cube is entirely in/out of the surface */
				if (edgeTable[iCubeIndex] == 0)
				{
					continue;
				}

				/* Find the vertices where the surface intersects the cube */
				if (edgeTable[iCubeIndex] & 1)
				{
					indlist[0] = m_pPreviousVertexIndicesX[uXRegSpace][uYRegSpace];
					//assert(indlist[0] != -1);
				}
				if (edgeTable[iCubeIndex] & 2)
				{
					indlist[1] = m_pPreviousVertexIndicesY[uXRegSpace+1][uYRegSpace];
					//assert(indlist[1] != -1);
				}
				if (edgeTable[iCubeIndex] & 4)
				{
					indlist[2] = m_pPreviousVertexIndicesX[uXRegSpace][uYRegSpace+1];
					//assert(indlist[2] != -1);
				}
				if (edgeTable[iCubeIndex] & 8)
				{
					indlist[3] = m_pPreviousVertexIndicesY[uXRegSpace][uYRegSpace];
					//assert(indlist[3] != -1);
				}
				if (edgeTable[iCubeIndex] & 16)
				{
					indlist[4] = m_pCurrentVertexIndicesX[uXRegSpace][uYRegSpace];
					//assert(indlist[4] != -1);
				}
				if (edgeTable[iCubeIndex] & 32)
				{
					indlist[5] = m_pCurrentVertexIndicesY[uXRegSpace+1][uYRegSpace];
					//assert(indlist[5] != -1);
				}
				if (edgeTable[iCubeIndex] & 64)
				{
					indlist[6] = m_pCurrentVertexIndicesX[uXRegSpace][uYRegSpace+1];
					//assert(indlist[6] != -1);
				}
				if (edgeTable[iCubeIndex] & 128)
				{
					indlist[7] = m_pCurrentVertexIndicesY[uXRegSpace][uYRegSpace];
					//assert(indlist[7] != -1);
				}
				if (edgeTable[iCubeIndex] & 256)
				{
					indlist[8] = m_pPreviousVertexIndicesZ[uXRegSpace][uYRegSpace];
					//assert(indlist[8] != -1);
				}
				if (edgeTable[iCubeIndex] & 512)
				{
					indlist[9] = m_pPreviousVertexIndicesZ[uXRegSpace+1][uYRegSpace];
					//assert(indlist[9] != -1);
				}
				if (edgeTable[iCubeIndex] & 1024)
				{
					indlist[10] = m_pPreviousVertexIndicesZ[uXRegSpace+1][uYRegSpace+1];
					//assert(indlist[10] != -1);
				}
				if (edgeTable[iCubeIndex] & 2048)
				{
					indlist[11] = m_pPreviousVertexIndicesZ[uXRegSpace][uYRegSpace+1];
					//assert(indlist[11] != -1);
				}

				for (int i=0;triTable[iCubeIndex][i]!=-1;i+=3)
				{
					int32_t ind0 = indlist[triTable[iCubeIndex][i  ]];
					int32_t ind1 = indlist[triTable[iCubeIndex][i+1]];
					int32_t ind2 = indlist[triTable[iCubeIndex][i+2]];

					if((ind0 != -1) && (ind1 != -1) && (ind2 != -1))
					{
						m_meshCurrent->addTriangle(ind0, ind1, ind2);
					}
				}//For each triangle
			}//For each cell
		}
	}
}
