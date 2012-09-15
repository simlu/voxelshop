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

#ifndef __PolyVox_LargeVolume_H__
#define __PolyVox_LargeVolume_H__

#include "PolyVoxCore/BaseVolume.h"
#include "PolyVoxImpl/Block.h"
#include "PolyVoxCore/Log.h"
#include "PolyVoxCore/Region.h"
#include "PolyVoxCore/Vector.h"

#include <limits>
#include <cassert>
#include <cstdlib> //For abort()
#include <cstring> //For memcpy
#include <list>
#include <map>
#include <memory>
#include <stdexcept> //For invalid_argument
#include <vector>

namespace PolyVox
{
	/// The LargeVolume class provides a memory efficient method of storing voxel data while also allowing fast access and modification.
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/// A LargeVolume is essentially a 3D array in which each element (or <i>voxel</i>) is identified by a three dimensional (x,y,z) coordinate.
	/// We use the LargeVolume class to store our data in an efficient way, and it is the input to many of the algorithms (such as the surface
	/// extractors) which form the heart of PolyVox. The LargeVolume class is templatised so that different types of data can be stored within each voxel.
	///
	/// <b> Basic usage</b>
	/// The following code snippet shows how to construct a volume and demonstrates basic usage:
	///
	/// \code
	/// LargeVolume<Material8> volume(Region(Vector3DInt32(0,0,0), Vector3DInt32(63,127,255)));
	/// volume.setVoxelAt(15, 90, 42, Material8(5));
	/// std::cout << "Voxel at (15, 90, 42) has value: " << volume.getVoxelAt(15, 90, 42).getMaterial() << std::endl;
	/// std::cout << "Width = " << volume.getWidth() << ", Height = " << volume.getHeight() << ", Depth = " << volume.getDepth() << std::endl;
	/// \endcode
	///
	/// In this particular example each voxel in the LargeVolume is of type 'Material8', as specified by the template parameter. This is one of several
	/// predefined voxel types, and it is also possible to define your own. The Material8 type simply holds an integer value where zero represents
	/// empty space and any other value represents a solid material.
	/// 
	/// The LargeVolume constructor takes a Region as a parameter. This specifies the valid range of voxels which can be held in the volume, so in this
	/// particular case the valid voxel positions are (0,0,0) to (63, 127, 255). Attempts to access voxels outside this range will result is accessing the
	/// border value (see getBorderValue() and setBorderValue()). PolyVox also has support for near infinite volumes which will be discussed later.
	/// 
	/// Access to individual voxels is provided via the setVoxelAt() and getVoxelAt() member functions. Advanced users may also be interested in
	/// the Sampler class for faster read-only access to a large number of voxels.
	/// 
	/// Lastly the example prints out some properties of the LargeVolume. Note that the dimentsions getWidth(), getHeight(), and getDepth() are inclusive, such
	/// that the width is 64 when the range of valid x coordinates goes from 0 to 63.
	/// 
	/// <b>Data Representaion</b>
	/// If stored carelessly, volume data can take up a huge amount of memory. For example, a volume of dimensions 1024x1024x1024 with
	/// 1 byte per voxel will require 1GB of memory if stored in an uncompressed form. Natuarally our LargeVolume class is much more efficient
	/// than this and it is worth understanding (at least at a high level) the approach which is used.
	///
	/// Essentially, the LargeVolume class stores its data as a collection of blocks. Each of these block is much smaller than the whole volume,
	/// for example a typical size might be 32x32x32 voxels (though is is configurable by the user). In this case, a 256x512x1024 volume
	/// would contain 8x16x32 = 4096 blocks. The data for each block is stored in a compressed form, which uses only a small amout of
	/// memory but it is hard to modify the data. Therefore, before any given voxel can be modified, its corresponding block must be uncompressed.
	///
	/// The compression and decompression of block is a relatively slow process and so we aim to do this as rarely as possible. In order
	/// to achive this, the volume class stores a cache of recently used blocks and their associated uncompressed data. Each time a voxel
	/// is touched a timestamp is updated on the corresponding block. When the cache becomes full the block with the oldest timestamp is
	/// recompressed and moved out of the cache.
	///
	/// <b>Achieving high compression rates</b>
	/// The compression rates which can be achieved can vary significantly depending the nature of the data you are storing, but you can
	/// encourage high compression rates by making your data as homogenous as possible. If you are simply storing a material with each
	/// voxel then this will probably happen naturally. Games such as Minecraft which use this approach will typically involve large areas
	/// of the same material which will compress down well.
	///
	/// However, if you are storing density values then you may want to take some care. The advantage of storing smoothly changing values
	/// is that you can get smooth surfaces extracted, but storing smoothly changing values inside or outside objects (rather than just
	/// on the boundary) does not benefit the surface and is very hard to compress effectively. You may wish to apply some thresholding to 
	/// your density values to reduce this problem (this threasholding should only be applied to voxels who don't contribute to the surface).
	///
	/// <b>Paging large volumes</b>
	/// The compression scheme described previously will typically allow you to load several billion voxels into a few hundred megabytes of memory, 
	/// though as explained the exact compression rate is highly dependant on your data. If you have more data than this then PolyVox provides a
	/// mechanism by which parts of the volume can be paged out of memory by calling user supplied callback functions. This mechanism allows a
	/// potentially unlimited amount of data to be loaded, provided the user is able to take responsibility for storing any data which PolyVox
	/// cannot fit in memory, and then returning it back to PolyVox on demand. For example, the user might choose to temporarily store this data
	/// on disk or stream it to a remote database.
	///
	/// You can construct such a LargeVolume as follows:
	///
	/// \code
	/// void myDataRequiredHandler(const ConstVolumeProxy<MaterialDensityPair44>& volume, const PolyVox::Region& reg)
	/// {
	///		//This function is being called because part of the data is missing from memory and needs to be supplied. The parameter
	///		//'volume' provides access to the volume data, and the parameter 'reg' indicates which region of the volume you need fill.	
	/// }
	///
	/// void myDataOverflowHandler(const ConstVolumeProxy<MaterialDensityPair44>& vol, const PolyVox::Region& reg)
	/// {
	///		//This function is being called because part of the data is about to be removed from memory. The parameter 'volume' 
	///		//provides access to the volume data, and the parameter 'reg' indicates which region of the volume you need to store.
	/// }
	///
	///	LargeVolume<Density>volData(&myDataRequiredHandler, &myDataOverflowHandler);
	/// \endcode
	///
	/// Essentially you are providing an extension to the LargeVolume class - a way for data to be stored once PolyVox has run out of memory for it. Note
	/// that you don't actually have to do anything with the data - you could simply decide that once it gets removed from memory it doesn't matter
	/// anymore. But you still need to be ready to then provide something to PolyVox (even if it's just default data) in the event that it is requested.
	///
	/// <b>Cache-aware traversal</b>
	/// You might be suprised at just how many cache misses can occur when you traverse the volume in a naive manner. Consider a 1024x1024x1024 volume
	/// with blocks of size 32x32x32. And imagine you iterate over this volume with a simple three-level for loop which iterates over x, the y, then z.
	/// If you start at position (0,0,0) then ny the time you reach position (1023,0,0) you have touched 1024 voxels along one edge of the volume and
	/// have pulled 32 blocks into the cache. By the time you reach (1023,1023,0) you have hit 1024x1024 voxels and pulled 32x32 blocks into the cache.
	/// You are now ready to touch voxel (0,0,1) which is right nect to where you started, but unless your cache is at least 32x32 blocks large then this
	/// initial block has already been cleared from the cache.
	///
	/// Ensuring you have a large enough cache size can obviously help the above situation, but you might also consider iterating over the voxels in a
	/// different order. For example, if you replace your three-level loop with a six-level loop then you can first process all the voxels between (0,0,0)
	/// and (31,31,31), then process all the voxels between (32,0,0) and (63,0,0), and so forth. Using this approach you will have no cache misses even
	/// is your cache sise is only one. Of course the logic is more complex, but writing code in such a cache-aware manner may be beneficial in some situations.
	///
	/// <b>Threading</b>
	/// The LargeVolume class does not make any guarentees about thread safety. You should ensure that all accesses are performed from the same thread.
	/// This is true even if you are only reading data from the volume, as concurrently reading from different threads can invalidate the contents
	/// of the block cache (amoung other problems).
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	template <typename VoxelType> class ConstVolumeProxy;

	template <typename VoxelType>
	class LargeVolume : public BaseVolume<VoxelType>
	{
	public:
		//There seems to be some descrepency between Visual Studio and GCC about how the following class should be declared.
		//There is a work around (see also See http://goo.gl/qu1wn) given below which appears to work on VS2010 and GCC, but
		//which seems to cause internal compiler errors on VS2008 when building with the /Gm 'Enable Minimal Rebuild' compiler
		//option. For now it seems best to 'fix' it with the preprocessor insstead, but maybe the workaround can be reinstated
		//in the future
		//typedef Volume<VoxelType> VolumeOfVoxelType; //Workaround for GCC/VS2010 differences.
		//class Sampler : public VolumeOfVoxelType::template Sampler< LargeVolume<VoxelType> >
#if defined(_MSC_VER)
		class Sampler : public BaseVolume<VoxelType>::Sampler< LargeVolume<VoxelType> > //This line works on VS2010
#else
                class Sampler : public BaseVolume<VoxelType>::template Sampler< LargeVolume<VoxelType> > //This line works on GCC
#endif
		{
		public:
			Sampler(LargeVolume<VoxelType>* volume);
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

		// Make the ConstVolumeProxy a friend
		friend class ConstVolumeProxy<VoxelType>;

		struct LoadedBlock
		{
		public:
			LoadedBlock(uint16_t uSideLength = 0)
				:block(uSideLength)
				,timestamp(0)
			{
			}

			Block<VoxelType> block;
			uint32_t timestamp;
		};

	public:		
		/// Constructor for creating a very large paging volume.
		LargeVolume
		(
			polyvox_function<void(const ConstVolumeProxy<VoxelType>&, const Region&)> dataRequiredHandler,
			polyvox_function<void(const ConstVolumeProxy<VoxelType>&, const Region&)> dataOverflowHandler,
			uint16_t uBlockSideLength = 32
		);
		/// Constructor for creating a fixed size volume.
		LargeVolume
		(
			const Region& regValid,
			polyvox_function<void(const ConstVolumeProxy<VoxelType>&, const Region&)> dataRequiredHandler = 0,
			polyvox_function<void(const ConstVolumeProxy<VoxelType>&, const Region&)> dataOverflowHandler = 0,
			bool bPagingEnabled = false,
			uint16_t uBlockSideLength = 32
		);
		/// Destructor
		~LargeVolume();

		/// Gets the value used for voxels which are outside the volume
		VoxelType getBorderValue(void) const;
		/// Gets a voxel at the position given by <tt>x,y,z</tt> coordinates
		VoxelType getVoxelAt(int32_t uXPos, int32_t uYPos, int32_t uZPos) const;
		/// Gets a voxel at the position given by a 3D vector
		VoxelType getVoxelAt(const Vector3DInt32& v3dPos) const;

		//Sets whether or not blocks are compressed in memory
		void setCompressionEnabled(bool bCompressionEnabled);
		/// Sets the number of blocks for which uncompressed data is stored
		void setMaxNumberOfUncompressedBlocks(uint32_t uMaxNumberOfUncompressedBlocks);
		/// Sets the number of blocks which can be in memory before the paging system starts unloading them
		void setMaxNumberOfBlocksInMemory(uint32_t uMaxNumberOfBlocksInMemory);
		/// Sets the value used for voxels which are outside the volume
		void setBorderValue(const VoxelType& tBorder);
		/// Sets the voxel at the position given by <tt>x,y,z</tt> coordinates
		bool setVoxelAt(int32_t uXPos, int32_t uYPos, int32_t uZPos, VoxelType tValue);
		/// Sets the voxel at the position given by a 3D vector
		bool setVoxelAt(const Vector3DInt32& v3dPos, VoxelType tValue);
		/// Tries to ensure that the voxels within the specified Region are loaded into memory.
		void prefetch(Region regPrefetch);
		/// Ensures that any voxels within the specified Region are removed from memory.
		void flush(Region regFlush);
		/// Removes all voxels from memory
		void flushAll();

		/// Empties the cache of uncompressed blocks
		void clearBlockCache(void);
		/// Calculates the approximate compression ratio of the store volume data
		float calculateCompressionRatio(void);
		/// Calculates approximatly how many bytes of memory the volume is currently using.
		uint32_t calculateSizeInBytes(void);

private:
		void initialise(const Region& regValidRegion, uint16_t uBlockSideLength);

		/// gets called when a new region is allocated and needs to be filled
		/// NOTE: accessing ANY voxels outside this region during the process of this function
		/// is absolutely unsafe
		polyvox_function<void(const ConstVolumeProxy<VoxelType>&, const Region&)> m_funcDataRequiredHandler;
		/// gets called when a Region needs to be stored by the user, because LargeVolume will erase it right after
		/// this function returns
		/// NOTE: accessing ANY voxels outside this region during the process of this function
		/// is absolutely unsafe
		polyvox_function<void(const ConstVolumeProxy<VoxelType>&, const Region&)> m_funcDataOverflowHandler;
	
		Block<VoxelType>* getUncompressedBlock(int32_t uBlockX, int32_t uBlockY, int32_t uBlockZ) const;
		void eraseBlock(typename std::map<Vector3DInt32, LoadedBlock >::iterator itBlock) const;
		/// this function can be called by m_funcDataRequiredHandler without causing any weird effects
		bool setVoxelAtConst(int32_t uXPos, int32_t uYPos, int32_t uZPos, VoxelType tValue) const;

		//The block data
		mutable std::map<Vector3DInt32, LoadedBlock > m_pBlocks;

		//The cache of uncompressed blocks. The uncompressed block data and the timestamps are stored here rather
		//than in the Block class. This is so that in the future each VolumeIterator might to maintain its own cache
		//of blocks. However, this could mean the same block data is uncompressed and modified in more than one
		//location in memory... could be messy with threading.
		mutable std::vector< LoadedBlock* > m_vecUncompressedBlockCache;
		mutable uint32_t m_uTimestamper;
		mutable Vector3DInt32 m_v3dLastAccessedBlockPos;
		mutable Block<VoxelType>* m_pLastAccessedBlock;
		uint32_t m_uMaxNumberOfUncompressedBlocks;
		uint32_t m_uMaxNumberOfBlocksInMemory;

		//We don't store an actual Block for the border, just the uncompressed data. This is partly because the border
		//block does not have a position (so can't be passed to getUncompressedBlock()) and partly because there's a
		//good chance we'll often hit it anyway. It's a chunk of homogenous data (rather than a single value) so that
		//the VolumeIterator can do it's usual pointer arithmetic without needing to know it's gone outside the volume.
		VoxelType* m_pUncompressedBorderData;

		//The size of the volume
		Region m_regValidRegionInBlocks;

		//The size of the blocks
		uint16_t m_uBlockSideLength;
		uint8_t m_uBlockSideLengthPower;

		bool m_bCompressionEnabled;
		bool m_bPagingEnabled;
	};
}

#include "PolyVoxCore/LargeVolume.inl"
#include "PolyVoxCore/LargeVolumeSampler.inl"

#endif //__PolyVox_LargeVolume_H__
