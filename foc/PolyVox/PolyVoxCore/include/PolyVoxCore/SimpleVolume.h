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

#ifndef __PolyVox_SimpleVolume_H__
#define __PolyVox_SimpleVolume_H__

#include "PolyVoxImpl/Utility.h"

#include "PolyVoxCore/BaseVolume.h"
#include "PolyVoxCore/Log.h"
#include "PolyVoxCore/Region.h"
#include "PolyVoxCore/Vector.h"

#include <cassert>
#include <cstdlib> //For abort()
#include <cstring> //For memcpy
#include <limits>
#include <memory>
#include <stdexcept> //For invalid_argument

namespace PolyVox
{
	template <typename VoxelType>
	class SimpleVolume : public BaseVolume<VoxelType>
	{
	public:
		#ifndef SWIG
		class Block
		{
		public:
			Block(uint16_t uSideLength = 0);
			~Block();

			uint16_t getSideLength(void) const;
			VoxelType getVoxelAt(uint16_t uXPos, uint16_t uYPos, uint16_t uZPos) const;
			VoxelType getVoxelAt(const Vector3DUint16& v3dPos) const;

			void setVoxelAt(uint16_t uXPos, uint16_t uYPos, uint16_t uZPos, VoxelType tValue);
			void setVoxelAt(const Vector3DUint16& v3dPos, VoxelType tValue);

			void fill(VoxelType tValue);
			void initialise(uint16_t uSideLength);
			uint32_t calculateSizeInBytes(void);

		public:
			VoxelType* m_tUncompressedData;
			uint16_t m_uSideLength;
			uint8_t m_uSideLengthPower;	
		};

		//There seems to be some descrepency between Visual Studio and GCC about how the following class should be declared.
		//There is a work around (see also See http://goo.gl/qu1wn) given below which appears to work on VS2010 and GCC, but
		//which seems to cause internal compiler errors on VS2008 when building with the /Gm 'Enable Minimal Rebuild' compiler
		//option. For now it seems best to 'fix' it with the preprocessor insstead, but maybe the workaround can be reinstated
		//in the future
		//typedef Volume<VoxelType> VolumeOfVoxelType; //Workaround for GCC/VS2010 differences.
		//class Sampler : public VolumeOfVoxelType::template Sampler< SimpleVolume<VoxelType> >
#if defined(_MSC_VER)
		class Sampler : public BaseVolume<VoxelType>::Sampler< SimpleVolume<VoxelType> > //This line works on VS2010
#else
                class Sampler : public BaseVolume<VoxelType>::template Sampler< SimpleVolume<VoxelType> > //This line works on GCC
#endif
		{
		public:
			Sampler(SimpleVolume<VoxelType>* volume);
			~Sampler();

			Sampler& operator=(const Sampler& rhs) throw();

			VoxelType getSubSampledVoxel(uint8_t uLevel) const;
			inline VoxelType getVoxel(void) const;			

			void setPosition(const Vector3DInt32& v3dNewPos);
			void setPosition(int32_t xPos, int32_t yPos, int32_t zPos);
			inline bool setVoxel(VoxelType tValue);

			void movePositiveX(void);
			void movePositiveY(void);
			void movePositiveZ(void);

			void moveNegativeX(void);
			void moveNegativeY(void);
			void moveNegativeZ(void);

			inline VoxelType peekVoxel1nx1ny1nz(void) const;
			inline VoxelType peekVoxel1nx1ny0pz(void) const;
			inline VoxelType peekVoxel1nx1ny1pz(void) const;
			inline VoxelType peekVoxel1nx0py1nz(void) const;
			inline VoxelType peekVoxel1nx0py0pz(void) const;
			inline VoxelType peekVoxel1nx0py1pz(void) const;
			inline VoxelType peekVoxel1nx1py1nz(void) const;
			inline VoxelType peekVoxel1nx1py0pz(void) const;
			inline VoxelType peekVoxel1nx1py1pz(void) const;

			inline VoxelType peekVoxel0px1ny1nz(void) const;
			inline VoxelType peekVoxel0px1ny0pz(void) const;
			inline VoxelType peekVoxel0px1ny1pz(void) const;
			inline VoxelType peekVoxel0px0py1nz(void) const;
			inline VoxelType peekVoxel0px0py0pz(void) const;
			inline VoxelType peekVoxel0px0py1pz(void) const;
			inline VoxelType peekVoxel0px1py1nz(void) const;
			inline VoxelType peekVoxel0px1py0pz(void) const;
			inline VoxelType peekVoxel0px1py1pz(void) const;

			inline VoxelType peekVoxel1px1ny1nz(void) const;
			inline VoxelType peekVoxel1px1ny0pz(void) const;
			inline VoxelType peekVoxel1px1ny1pz(void) const;
			inline VoxelType peekVoxel1px0py1nz(void) const;
			inline VoxelType peekVoxel1px0py0pz(void) const;
			inline VoxelType peekVoxel1px0py1pz(void) const;
			inline VoxelType peekVoxel1px1py1nz(void) const;
			inline VoxelType peekVoxel1px1py0pz(void) const;
			inline VoxelType peekVoxel1px1py1pz(void) const;

		private:			
			//Other current position information
			VoxelType* mCurrentVoxel;
		};
		#endif

	public:
		/// Constructor for creating a fixed size volume.
		SimpleVolume
		(
			const Region& regValid,
			uint16_t uBlockSideLength = 32
		);
		/// Destructor
		~SimpleVolume();

		/// Gets the value used for voxels which are outside the volume
		VoxelType getBorderValue(void) const;
		/// Gets a voxel at the position given by <tt>x,y,z</tt> coordinates
		VoxelType getVoxelAt(int32_t uXPos, int32_t uYPos, int32_t uZPos) const;
		/// Gets a voxel at the position given by a 3D vector
		VoxelType getVoxelAt(const Vector3DInt32& v3dPos) const;

		/// Sets the value used for voxels which are outside the volume
		void setBorderValue(const VoxelType& tBorder);
		/// Sets the voxel at the position given by <tt>x,y,z</tt> coordinates
		bool setVoxelAt(int32_t uXPos, int32_t uYPos, int32_t uZPos, VoxelType tValue);
		/// Sets the voxel at the position given by a 3D vector
		bool setVoxelAt(const Vector3DInt32& v3dPos, VoxelType tValue);

		/// Calculates approximatly how many bytes of memory the volume is currently using.
		uint32_t calculateSizeInBytes(void);

private:	
		void initialise(const Region& regValidRegion, uint16_t uBlockSideLength);

		Block* getUncompressedBlock(int32_t uBlockX, int32_t uBlockY, int32_t uBlockZ) const;

		//The block data
		Block* m_pBlocks;

		//We don't store an actual Block for the border, just the uncompressed data. This is partly because the border
		//block does not have a position (so can't be passed to getUncompressedBlock()) and partly because there's a
		//good chance we'll often hit it anyway. It's a chunk of homogenous data (rather than a single value) so that
		//the VolumeIterator can do it's usual pointer arithmetic without needing to know it's gone outside the volume.
		VoxelType* m_pUncompressedBorderData;

		//The size of the volume in vlocks
		Region m_regValidRegionInBlocks;

		//Volume size measured in blocks.
		uint32_t m_uNoOfBlocksInVolume;
		uint16_t m_uWidthInBlocks;
		uint16_t m_uHeightInBlocks;
		uint16_t m_uDepthInBlocks;

		//The size of the blocks
		uint32_t m_uNoOfVoxelsPerBlock;
		uint16_t m_uBlockSideLength;
		uint8_t m_uBlockSideLengthPower;
	};
}

#include "PolyVoxCore/SimpleVolumeBlock.inl"
#include "PolyVoxCore/SimpleVolume.inl"
#include "PolyVoxCore/SimpleVolumeSampler.inl"

#endif //__PolyVox_SimpleVolume_H__
