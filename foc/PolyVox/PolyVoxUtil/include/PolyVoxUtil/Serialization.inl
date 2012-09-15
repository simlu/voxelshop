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
	//Note: we don't do much error handling in here - exceptions will simply be propergated up to the caller.
	//FIXME - think about pointer ownership issues. Or could return volume by value if the copy constructor is shallow
	template< typename VolumeType >
	polyvox_shared_ptr< VolumeType > loadVolumeRaw(std::istream& stream, VolumeSerializationProgressListener* progressListener)
	{
		assert(false); //THIS FUNCTION IS DEPRECATED. REMOVE THIS ASSERT TO CONTINUE, BUT SWITCH TO 'loadVolume()' ASAP.

		//Read volume dimensions
		uint8_t volumeWidthPower = 0;
		uint8_t volumeHeightPower = 0;
		uint8_t volumeDepthPower = 0;
		stream.read(reinterpret_cast<char*>(&volumeWidthPower), sizeof(volumeWidthPower));
		stream.read(reinterpret_cast<char*>(&volumeHeightPower), sizeof(volumeHeightPower));
		stream.read(reinterpret_cast<char*>(&volumeDepthPower), sizeof(volumeDepthPower));

		uint16_t volumeWidth = 0x0001 << volumeWidthPower;
		uint16_t volumeHeight = 0x0001 << volumeHeightPower;
		uint16_t volumeDepth = 0x0001 << volumeDepthPower;

		//FIXME - need to support non cubic volumes
		polyvox_shared_ptr< VolumeType > volume(new LargeVolume<VolumeType::VoxelType>(volumeWidth, volumeHeight, volumeDepth));

		//Read data
		for(uint16_t z = 0; z < volumeDepth; ++z)
		{
			//Update progress once per slice.
			if(progressListener)
			{
				float fProgress = static_cast<float>(z) / static_cast<float>(volumeDepth);
				progressListener->onProgressUpdated(fProgress);
			}

			for(uint16_t y = 0; y < volumeHeight; ++y)
			{
				for(uint16_t x = 0; x < volumeWidth; ++x)
				{
					VolumeType::VoxelType value;
					stream.read(reinterpret_cast<char*>(&value), sizeof(value));

					volume->setVoxelAt(x,y,z,value);
				}
			}
		}

		//Finished
		if(progressListener)
		{
			progressListener->onProgressUpdated(1.0f);
		}

		return volume;
	}

	template< typename VolumeType >
	void saveVolumeRaw(std::ostream& stream, VolumeType& volume, VolumeSerializationProgressListener* progressListener)
	{
		assert(false); //THIS FUNCTION IS DEPRECATED. REMOVE THIS ASSERT TO CONTINUE, BUT SWITCH TO 'saveVolume()' ASAP.

		//Write volume dimensions
		uint16_t volumeWidth = volume.getWidth();
		uint16_t volumeHeight = volume.getHeight();
		uint16_t volumeDepth  = volume.getDepth();

		uint8_t volumeWidthPower = logBase2(volumeWidth);
		uint8_t volumeHeightPower = logBase2(volumeHeight);
		uint8_t volumeDepthPower = logBase2(volumeDepth);

		stream.write(reinterpret_cast<char*>(&volumeWidthPower), sizeof(volumeWidthPower));
		stream.write(reinterpret_cast<char*>(&volumeHeightPower), sizeof(volumeHeightPower));
		stream.write(reinterpret_cast<char*>(&volumeDepthPower), sizeof(volumeDepthPower));

		//Write data
		VolumeType::Sampler volIter(&volume);
		for(uint16_t z = 0; z < volumeDepth; ++z)
		{
			//Update progress once per slice.
			if(progressListener)
			{
				float fProgress = static_cast<float>(z) / static_cast<float>(volumeDepth);
				progressListener->onProgressUpdated(fProgress);
			}

			for(uint16_t y = 0; y < volumeHeight; ++y)
			{
				for(uint16_t x = 0; x < volumeWidth; ++x)
				{
					volIter.setPosition(x,y,z);
					VolumeType::VoxelType value = volIter.getVoxel();
					stream.write(reinterpret_cast<char*>(&value), sizeof(value));
				}
			}
		}

		//Finished
		if(progressListener)
		{
			progressListener->onProgressUpdated(1.0f);
		}
	}

	//Note: we don't do much error handling in here - exceptions will simply be propergated up to the caller.
	//FIXME - think about pointer ownership issues. Or could return volume by value if the copy constructor is shallow
	template< typename VolumeType >
	polyvox_shared_ptr< VolumeType > loadVolumeRle(std::istream& stream, VolumeSerializationProgressListener* progressListener)
	{
		assert(false); //THIS FUNCTION IS DEPRECATED. REMOVE THIS ASSERT TO CONTINUE, BUT SWITCH TO 'loadVolume()' ASAP.

		//Read volume dimensions
		uint8_t volumeWidthPower = 0;
		uint8_t volumeHeightPower = 0;
		uint8_t volumeDepthPower = 0;
		stream.read(reinterpret_cast<char*>(&volumeWidthPower), sizeof(volumeWidthPower));
		stream.read(reinterpret_cast<char*>(&volumeHeightPower), sizeof(volumeHeightPower));
		stream.read(reinterpret_cast<char*>(&volumeDepthPower), sizeof(volumeDepthPower));

		uint16_t volumeWidth = 0x0001 << volumeWidthPower;
		uint16_t volumeHeight = 0x0001 << volumeHeightPower;
		uint16_t volumeDepth = 0x0001 << volumeDepthPower;

		//FIXME - need to support non cubic volumes
		polyvox_shared_ptr< VolumeType > volume(new LargeVolume<VolumeType::VoxelType>(volumeWidth, volumeHeight, volumeDepth));

		//Read data
		bool firstTime = true;
		uint32_t runLength = 0;
		VolumeType::VoxelType value;
		stream.read(reinterpret_cast<char*>(&value), sizeof(value));
		stream.read(reinterpret_cast<char*>(&runLength), sizeof(runLength));
		for(uint16_t z = 0; z < volumeDepth; ++z)
		{
			//Update progress once per slice.
			if(progressListener)
			{
				float fProgress = static_cast<float>(z) / static_cast<float>(volumeDepth);
				progressListener->onProgressUpdated(fProgress);
			}

			for(uint16_t y = 0; y < volumeHeight; ++y)
			{
				for(uint16_t x = 0; x < volumeWidth; ++x)
				{	
					if(runLength != 0)
					{
						volume->setVoxelAt(x,y,z,value);
						runLength--;
					}
					else
					{
						stream.read(reinterpret_cast<char*>(&value), sizeof(value));
						stream.read(reinterpret_cast<char*>(&runLength), sizeof(runLength));

						volume->setVoxelAt(x,y,z,value);
						runLength--;
					}
				}
			}			
		}

		//Finished
		if(progressListener)
		{
			progressListener->onProgressUpdated(1.0f);
		}

		return volume;
	}

	template< typename VolumeType >
	void saveVolumeRle(std::ostream& stream, VolumeType& volume, VolumeSerializationProgressListener* progressListener)
	{
		assert(false); //THIS FUNCTION IS DEPRECATED. REMOVE THIS ASSERT TO CONTINUE, BUT SWITCH TO 'saveVolume()' ASAP.

		//Write volume dimensions
		uint16_t volumeWidth = volume.getWidth();
		uint16_t volumeHeight = volume.getHeight();
		uint16_t volumeDepth  = volume.getDepth();

		uint8_t volumeWidthPower = logBase2(volumeWidth);
		uint8_t volumeHeightPower = logBase2(volumeHeight);
		uint8_t volumeDepthPower = logBase2(volumeDepth);

		stream.write(reinterpret_cast<char*>(&volumeWidthPower), sizeof(volumeWidthPower));
		stream.write(reinterpret_cast<char*>(&volumeHeightPower), sizeof(volumeHeightPower));
		stream.write(reinterpret_cast<char*>(&volumeDepthPower), sizeof(volumeDepthPower));

		//Write data
		VolumeType::Sampler volIter(&volume);
		VolumeType::VoxelType current;
		uint32_t runLength = 0;
		bool firstTime = true;
		for(uint16_t z = 0; z < volumeDepth; ++z)
		{
			//Update progress once per slice.
			if(progressListener)
			{
				float fProgress = static_cast<float>(z) / static_cast<float>(volumeDepth);
				progressListener->onProgressUpdated(fProgress);
			}

			for(uint16_t y = 0; y < volumeHeight; ++y)
			{
				for(uint16_t x = 0; x < volumeWidth; ++x)
				{		
					volIter.setPosition(x,y,z);
					VolumeType::VoxelType value = volIter.getVoxel();
					if(firstTime)
					{
						current = value;
						runLength = 1;
						firstTime = false;
					}
					else
					{
						if(value == current)
						{
							runLength++;
						}
						else
						{
							stream.write(reinterpret_cast<char*>(&current), sizeof(current));
							stream.write(reinterpret_cast<char*>(&runLength), sizeof(runLength));
							current = value;
							runLength = 1;
						}
					}					
				}
			}
		}
		stream.write(reinterpret_cast<char*>(&current), sizeof(current));
		stream.write(reinterpret_cast<char*>(&runLength), sizeof(runLength));

		//Finished
		if(progressListener)
		{
			progressListener->onProgressUpdated(1.0f);
		}
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////
	// New version of load/save code with versioning
	////////////////////////////////////////////////////////////////////////////////////////////////////

	template< typename VolumeType >
	bool loadVolume(std::istream& stream, VolumeType& volume, VolumeSerializationProgressListener* progressListener)
	{
		char pIdentifier[8];
		stream.read(pIdentifier, 7);
		pIdentifier[7] = '\0'; //Set the null terminator
		if(strcmp(pIdentifier, "PolyVox") != 0)
		{
			return false;
		}

		uint16_t uVersion;
		stream.read(reinterpret_cast<char*>(&uVersion), sizeof(uVersion));

		switch(uVersion)
		{
			case 0:
				return loadVersion0(stream, volume, progressListener);
				//Return means no need to break...
			default:
				return false;
		}
		
	}

	template< typename VolumeType >
	bool saveVolume(std::ostream& stream, VolumeType& volume, VolumeSerializationProgressListener* progressListener)
	{
		char pIdentifier[] = "PolyVox";
		stream.write(pIdentifier, 7);

		uint16_t uVersion = 0;
		stream.write(reinterpret_cast<const char*>(&uVersion), sizeof(uVersion));

		return saveVersion0(stream, volume, progressListener);
	}

	//Note: we don't do much error handling in here - exceptions will simply be propergated up to the caller.
	//FIXME - think about pointer ownership issues. Or could return volume by value if the copy constructor is shallow
	template< typename VolumeType >
	bool loadVersion0(std::istream& stream, VolumeType& volume, VolumeSerializationProgressListener* progressListener)
	{
		//Read volume dimensions
		uint16_t volumeWidth = 0;
		uint16_t volumeHeight = 0;
		uint16_t volumeDepth = 0;
		stream.read(reinterpret_cast<char*>(&volumeWidth), sizeof(volumeWidth));
		stream.read(reinterpret_cast<char*>(&volumeHeight), sizeof(volumeHeight));
		stream.read(reinterpret_cast<char*>(&volumeDepth), sizeof(volumeDepth));

		//Resize the volume
		//HACK - Forces block size to 32. This functions needs reworking anyway due to large volume support.
		volume.resize(Region(Vector3DInt32(0,0,0), Vector3DInt32(volumeWidth-1, volumeHeight-1, volumeDepth-1)), 32);

		//Read data
		bool firstTime = true;
		uint32_t runLength = 0;
		VolumeType::VoxelType value;
		stream.read(reinterpret_cast<char*>(&value), sizeof(value));
		stream.read(reinterpret_cast<char*>(&runLength), sizeof(runLength));
		for(uint16_t z = 0; z < volumeDepth; ++z)
		{
			//Update progress once per slice.
			if(progressListener)
			{
				float fProgress = static_cast<float>(z) / static_cast<float>(volumeDepth);
				progressListener->onProgressUpdated(fProgress);
			}

			for(uint16_t y = 0; y < volumeHeight; ++y)
			{
				for(uint16_t x = 0; x < volumeWidth; ++x)
				{	
					if(runLength != 0)
					{
						volume.setVoxelAt(x,y,z,value);
						runLength--;
					}
					else
					{
						stream.read(reinterpret_cast<char*>(&value), sizeof(value));
						stream.read(reinterpret_cast<char*>(&runLength), sizeof(runLength));

						volume.setVoxelAt(x,y,z,value);
						runLength--;
					}
				}
			}			
		}

		//Finished
		if(progressListener)
		{
			progressListener->onProgressUpdated(1.0f);
		}

		return true;
	}

	template< typename VolumeType >
	bool saveVersion0(std::ostream& stream, VolumeType& volume, VolumeSerializationProgressListener* progressListener)
	{
		//Write volume dimensions
		uint16_t volumeWidth = volume.getWidth();
		uint16_t volumeHeight = volume.getHeight();
		uint16_t volumeDepth  = volume.getDepth();

		stream.write(reinterpret_cast<char*>(&volumeWidth), sizeof(volumeWidth));
		stream.write(reinterpret_cast<char*>(&volumeHeight), sizeof(volumeHeight));
		stream.write(reinterpret_cast<char*>(&volumeDepth), sizeof(volumeDepth));

		//Write data
		VolumeType::Sampler volIter(&volume);
		VolumeType::VoxelType current;
		uint32_t runLength = 0;
		bool firstTime = true;
		for(uint16_t z = 0; z < volumeDepth; ++z)
		{
			//Update progress once per slice.
			if(progressListener)
			{
				float fProgress = static_cast<float>(z) / static_cast<float>(volumeDepth);
				progressListener->onProgressUpdated(fProgress);
			}

			for(uint16_t y = 0; y < volumeHeight; ++y)
			{
				for(uint16_t x = 0; x < volumeWidth; ++x)
				{		
					volIter.setPosition(x,y,z);
					VolumeType::VoxelType value = volIter.getVoxel();
					if(firstTime)
					{
						current = value;
						runLength = 1;
						firstTime = false;
					}
					else
					{
						if(value == current)
						{
							runLength++;
						}
						else
						{
							stream.write(reinterpret_cast<char*>(&current), sizeof(current));
							stream.write(reinterpret_cast<char*>(&runLength), sizeof(runLength));
							current = value;
							runLength = 1;
						}
					}					
				}
			}
		}
		stream.write(reinterpret_cast<char*>(&current), sizeof(current));
		stream.write(reinterpret_cast<char*>(&runLength), sizeof(runLength));

		//Finished
		if(progressListener)
		{
			progressListener->onProgressUpdated(1.0f);
		}

		return true;
	}
}
