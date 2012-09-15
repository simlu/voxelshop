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

#define BORDER_LOW(x) ((( x >> this->mVolume->m_uBlockSideLengthPower) << this->mVolume->m_uBlockSideLengthPower) != x)
#define BORDER_HIGH(x) ((( (x+1) >> this->mVolume->m_uBlockSideLengthPower) << this->mVolume->m_uBlockSideLengthPower) != (x+1))
//#define BORDER_LOW(x) (( x % mVolume->m_uBlockSideLength) != 0)
//#define BORDER_HIGH(x) (( x % mVolume->m_uBlockSideLength) != mVolume->m_uBlockSideLength - 1)

namespace PolyVox
{
	template <typename VoxelType>
	LargeVolume<VoxelType>::Sampler::Sampler(LargeVolume<VoxelType>* volume)
		:BaseVolume<VoxelType>::template Sampler< LargeVolume<VoxelType> >(volume)
	{
	}

	template <typename VoxelType>
	LargeVolume<VoxelType>::Sampler::~Sampler()
	{
	}

	template <typename VoxelType>
	typename LargeVolume<VoxelType>::Sampler& LargeVolume<VoxelType>::Sampler::operator=(const typename LargeVolume<VoxelType>::Sampler& rhs) throw()
	{
		if(this == &rhs)
		{
			return *this;
		}
        this->mVolume = rhs.mVolume;
		this->mXPosInVolume = rhs.mXPosInVolume;
		this->mYPosInVolume = rhs.mYPosInVolume;
		this->mZPosInVolume = rhs.mZPosInVolume;
		mCurrentVoxel = rhs.mCurrentVoxel;
        return *this;
	}

	template <typename VoxelType>
	VoxelType LargeVolume<VoxelType>::Sampler::getSubSampledVoxel(uint8_t uLevel) const
	{		
		if(uLevel == 0)
		{
			return getVoxel();
		}
		else if(uLevel == 1)
		{
			VoxelType tValue = getVoxel();
			tValue = (std::min)(tValue, peekVoxel1px0py0pz());
			tValue = (std::min)(tValue, peekVoxel0px1py0pz());
			tValue = (std::min)(tValue, peekVoxel1px1py0pz());
			tValue = (std::min)(tValue, peekVoxel0px0py1pz());
			tValue = (std::min)(tValue, peekVoxel1px0py1pz());
			tValue = (std::min)(tValue, peekVoxel0px1py1pz());
			tValue = (std::min)(tValue, peekVoxel1px1py1pz());
			return tValue;
		}
		else
		{
			const uint8_t uSize = 1 << uLevel;

			VoxelType tValue = (std::numeric_limits<VoxelType>::max)();
			for(uint8_t z = 0; z < uSize; ++z)
			{
				for(uint8_t y = 0; y < uSize; ++y)
				{
					for(uint8_t x = 0; x < uSize; ++x)
					{
						tValue = (std::min)(tValue, this->mVolume->getVoxelAt(this->mXPosInVolume + x, this->mYPosInVolume + y, this->mZPosInVolume + z));
					}
				}
			}
			return tValue;
		}
	}

	template <typename VoxelType>
	VoxelType LargeVolume<VoxelType>::Sampler::getVoxel(void) const
	{
		return *mCurrentVoxel;
	}

	template <typename VoxelType>
	void LargeVolume<VoxelType>::Sampler::setPosition(const Vector3DInt32& v3dNewPos)
	{
		setPosition(v3dNewPos.getX(), v3dNewPos.getY(), v3dNewPos.getZ());
	}

	template <typename VoxelType>
	void LargeVolume<VoxelType>::Sampler::setPosition(int32_t xPos, int32_t yPos, int32_t zPos)
	{
		this->mXPosInVolume = xPos;
		this->mYPosInVolume = yPos;
		this->mZPosInVolume = zPos;

		const int32_t uXBlock = this->mXPosInVolume >> this->mVolume->m_uBlockSideLengthPower;
		const int32_t uYBlock = this->mYPosInVolume >> this->mVolume->m_uBlockSideLengthPower;
		const int32_t uZBlock = this->mZPosInVolume >> this->mVolume->m_uBlockSideLengthPower;

		const uint16_t uXPosInBlock = static_cast<uint16_t>(this->mXPosInVolume - (uXBlock << this->mVolume->m_uBlockSideLengthPower));
		const uint16_t uYPosInBlock = static_cast<uint16_t>(this->mYPosInVolume - (uYBlock << this->mVolume->m_uBlockSideLengthPower));
		const uint16_t uZPosInBlock = static_cast<uint16_t>(this->mZPosInVolume - (uZBlock << this->mVolume->m_uBlockSideLengthPower));

		const uint32_t uVoxelIndexInBlock = uXPosInBlock + 
				uYPosInBlock * this->mVolume->m_uBlockSideLength + 
				uZPosInBlock * this->mVolume->m_uBlockSideLength * this->mVolume->m_uBlockSideLength;

		if(this->mVolume->m_regValidRegionInBlocks.containsPoint(Vector3DInt32(uXBlock, uYBlock, uZBlock)))
		{
			Block<VoxelType>* pUncompressedCurrentBlock = this->mVolume->getUncompressedBlock(uXBlock, uYBlock, uZBlock);

			mCurrentVoxel = pUncompressedCurrentBlock->m_tUncompressedData + uVoxelIndexInBlock;
		}
		else
		{
			mCurrentVoxel = this->mVolume->m_pUncompressedBorderData + uVoxelIndexInBlock;
		}
	}

	template <typename VoxelType>
	bool LargeVolume<VoxelType>::Sampler::setVoxel(VoxelType tValue)
	{
		//*mCurrentVoxel = tValue;
		//Need to think what effect this has on any existing iterators.
		assert(false);
		return false;
	}

	template <typename VoxelType>
	void LargeVolume<VoxelType>::Sampler::movePositiveX(void)
	{
		//Note the *pre* increament here
		if((++this->mXPosInVolume) % this->mVolume->m_uBlockSideLength != 0)
		{
			//No need to compute new block.
			++mCurrentVoxel;			
		}
		else
		{
			//We've hit the block boundary. Just calling setPosition() is the easiest way to resolve this.
			setPosition(this->mXPosInVolume, this->mYPosInVolume, this->mZPosInVolume);
		}
	}

	template <typename VoxelType>
	void LargeVolume<VoxelType>::Sampler::movePositiveY(void)
	{
		//Note the *pre* increament here
		if((++this->mYPosInVolume) % this->mVolume->m_uBlockSideLength != 0)
		{
			//No need to compute new block.
			mCurrentVoxel += this->mVolume->m_uBlockSideLength;
		}
		else
		{
			//We've hit the block boundary. Just calling setPosition() is the easiest way to resolve this.
			setPosition(this->mXPosInVolume, this->mYPosInVolume, this->mZPosInVolume);
		}
	}

	template <typename VoxelType>
	void LargeVolume<VoxelType>::Sampler::movePositiveZ(void)
	{
		//Note the *pre* increament here
		if((++this->mZPosInVolume) % this->mVolume->m_uBlockSideLength != 0)
		{
			//No need to compute new block.
			mCurrentVoxel += this->mVolume->m_uBlockSideLength * this->mVolume->m_uBlockSideLength;
		}
		else
		{
			//We've hit the block boundary. Just calling setPosition() is the easiest way to resolve this.
			setPosition(this->mXPosInVolume, this->mYPosInVolume, this->mZPosInVolume);
		}
	}

	template <typename VoxelType>
	void LargeVolume<VoxelType>::Sampler::moveNegativeX(void)
	{
		//Note the *post* decreament here
		if((this->mXPosInVolume--) % this->mVolume->m_uBlockSideLength != 0)
		{
			//No need to compute new block.
			--mCurrentVoxel;			
		}
		else
		{
			//We've hit the block boundary. Just calling setPosition() is the easiest way to resolve this.
			setPosition(this->mXPosInVolume, this->mYPosInVolume, this->mZPosInVolume);
		}
	}

	template <typename VoxelType>
	void LargeVolume<VoxelType>::Sampler::moveNegativeY(void)
	{
		//Note the *post* decreament here
		if((this->mYPosInVolume--) % this->mVolume->m_uBlockSideLength != 0)
		{
			//No need to compute new block.
			mCurrentVoxel -= this->mVolume->m_uBlockSideLength;
		}
		else
		{
			//We've hit the block boundary. Just calling setPosition() is the easiest way to resolve this.
			setPosition(this->mXPosInVolume, this->mYPosInVolume, this->mZPosInVolume);
		}
	}

	template <typename VoxelType>
	void LargeVolume<VoxelType>::Sampler::moveNegativeZ(void)
	{
		//Note the *post* decreament here
		if((this->mZPosInVolume--) % this->mVolume->m_uBlockSideLength != 0)
		{
			//No need to compute new block.
			mCurrentVoxel -= this->mVolume->m_uBlockSideLength * this->mVolume->m_uBlockSideLength;
		}
		else
		{
			//We've hit the block boundary. Just calling setPosition() is the easiest way to resolve this.
			setPosition(this->mXPosInVolume, this->mYPosInVolume, this->mZPosInVolume);
		}
	}

	template <typename VoxelType>
	VoxelType LargeVolume<VoxelType>::Sampler::peekVoxel1nx1ny1nz(void) const
	{
		if(	BORDER_LOW(this->mXPosInVolume) && BORDER_LOW(this->mYPosInVolume) && BORDER_LOW(this->mZPosInVolume) )
		{
			return *(mCurrentVoxel - 1 - this->mVolume->m_uBlockSideLength - this->mVolume->m_uBlockSideLength*this->mVolume->m_uBlockSideLength);
		}
		return this->mVolume->getVoxelAt(this->mXPosInVolume-1,this->mYPosInVolume-1,this->mZPosInVolume-1);
	}

	template <typename VoxelType>
	VoxelType LargeVolume<VoxelType>::Sampler::peekVoxel1nx1ny0pz(void) const
	{
		if(	BORDER_LOW(this->mXPosInVolume) && BORDER_LOW(this->mYPosInVolume) )
		{
			return *(mCurrentVoxel - 1 - this->mVolume->m_uBlockSideLength);
		}
		return this->mVolume->getVoxelAt(this->mXPosInVolume-1,this->mYPosInVolume-1,this->mZPosInVolume);
	}

	template <typename VoxelType>
	VoxelType LargeVolume<VoxelType>::Sampler::peekVoxel1nx1ny1pz(void) const
	{
		if(	BORDER_LOW(this->mXPosInVolume) && BORDER_LOW(this->mYPosInVolume) && BORDER_HIGH(this->mZPosInVolume) )
		{
			return *(mCurrentVoxel - 1 - this->mVolume->m_uBlockSideLength + this->mVolume->m_uBlockSideLength*this->mVolume->m_uBlockSideLength);
		}
		return this->mVolume->getVoxelAt(this->mXPosInVolume-1,this->mYPosInVolume-1,this->mZPosInVolume+1);
	}

	template <typename VoxelType>
	VoxelType LargeVolume<VoxelType>::Sampler::peekVoxel1nx0py1nz(void) const
	{
		if(	BORDER_LOW(this->mXPosInVolume) && BORDER_LOW(this->mZPosInVolume) )
		{
			return *(mCurrentVoxel - 1 - this->mVolume->m_uBlockSideLength*this->mVolume->m_uBlockSideLength);
		}
		return this->mVolume->getVoxelAt(this->mXPosInVolume-1,this->mYPosInVolume,this->mZPosInVolume-1);
	}

	template <typename VoxelType>
	VoxelType LargeVolume<VoxelType>::Sampler::peekVoxel1nx0py0pz(void) const
	{
		if( BORDER_LOW(this->mXPosInVolume) )
		{
			return *(mCurrentVoxel - 1);
		}
		return this->mVolume->getVoxelAt(this->mXPosInVolume-1,this->mYPosInVolume,this->mZPosInVolume);
	}

	template <typename VoxelType>
	VoxelType LargeVolume<VoxelType>::Sampler::peekVoxel1nx0py1pz(void) const
	{
		if( BORDER_LOW(this->mXPosInVolume) && BORDER_HIGH(this->mZPosInVolume) )
		{
			return *(mCurrentVoxel - 1 + this->mVolume->m_uBlockSideLength*this->mVolume->m_uBlockSideLength);
		}
		return this->mVolume->getVoxelAt(this->mXPosInVolume-1,this->mYPosInVolume,this->mZPosInVolume+1);
	}

	template <typename VoxelType>
	VoxelType LargeVolume<VoxelType>::Sampler::peekVoxel1nx1py1nz(void) const
	{
		if( BORDER_LOW(this->mXPosInVolume) && BORDER_HIGH(this->mYPosInVolume) && BORDER_LOW(this->mZPosInVolume) )
		{
			return *(mCurrentVoxel - 1 + this->mVolume->m_uBlockSideLength - this->mVolume->m_uBlockSideLength*this->mVolume->m_uBlockSideLength);
		}
		return this->mVolume->getVoxelAt(this->mXPosInVolume-1,this->mYPosInVolume+1,this->mZPosInVolume-1);
	}

	template <typename VoxelType>
	VoxelType LargeVolume<VoxelType>::Sampler::peekVoxel1nx1py0pz(void) const
	{
		if( BORDER_LOW(this->mXPosInVolume) && BORDER_HIGH(this->mYPosInVolume) )
		{
			return *(mCurrentVoxel - 1 + this->mVolume->m_uBlockSideLength);
		}
		return this->mVolume->getVoxelAt(this->mXPosInVolume-1,this->mYPosInVolume+1,this->mZPosInVolume);
	}

	template <typename VoxelType>
	VoxelType LargeVolume<VoxelType>::Sampler::peekVoxel1nx1py1pz(void) const
	{
		if( BORDER_LOW(this->mXPosInVolume) && BORDER_HIGH(this->mYPosInVolume) && BORDER_HIGH(this->mZPosInVolume) )
		{
			return *(mCurrentVoxel - 1 + this->mVolume->m_uBlockSideLength + this->mVolume->m_uBlockSideLength*this->mVolume->m_uBlockSideLength);
		}
		return this->mVolume->getVoxelAt(this->mXPosInVolume-1,this->mYPosInVolume+1,this->mZPosInVolume+1);
	}

	//////////////////////////////////////////////////////////////////////////

	template <typename VoxelType>
	VoxelType LargeVolume<VoxelType>::Sampler::peekVoxel0px1ny1nz(void) const
	{
		if( BORDER_LOW(this->mYPosInVolume) && BORDER_LOW(this->mZPosInVolume) )
		{
			return *(mCurrentVoxel - this->mVolume->m_uBlockSideLength - this->mVolume->m_uBlockSideLength*this->mVolume->m_uBlockSideLength);
		}
		return this->mVolume->getVoxelAt(this->mXPosInVolume,this->mYPosInVolume-1,this->mZPosInVolume-1);
	}

	template <typename VoxelType>
	VoxelType LargeVolume<VoxelType>::Sampler::peekVoxel0px1ny0pz(void) const
	{
		if( BORDER_LOW(this->mYPosInVolume) )
		{
			return *(mCurrentVoxel - this->mVolume->m_uBlockSideLength);
		}
		return this->mVolume->getVoxelAt(this->mXPosInVolume,this->mYPosInVolume-1,this->mZPosInVolume);
	}

	template <typename VoxelType>
	VoxelType LargeVolume<VoxelType>::Sampler::peekVoxel0px1ny1pz(void) const
	{
		if( BORDER_LOW(this->mYPosInVolume) && BORDER_HIGH(this->mZPosInVolume) )
		{
			return *(mCurrentVoxel - this->mVolume->m_uBlockSideLength + this->mVolume->m_uBlockSideLength*this->mVolume->m_uBlockSideLength);
		}
		return this->mVolume->getVoxelAt(this->mXPosInVolume,this->mYPosInVolume-1,this->mZPosInVolume+1);
	}

	template <typename VoxelType>
	VoxelType LargeVolume<VoxelType>::Sampler::peekVoxel0px0py1nz(void) const
	{
		if( BORDER_LOW(this->mZPosInVolume) )
		{
			return *(mCurrentVoxel - this->mVolume->m_uBlockSideLength*this->mVolume->m_uBlockSideLength);
		}
		return this->mVolume->getVoxelAt(this->mXPosInVolume,this->mYPosInVolume,this->mZPosInVolume-1);
	}

	template <typename VoxelType>
	VoxelType LargeVolume<VoxelType>::Sampler::peekVoxel0px0py0pz(void) const
	{
			return *mCurrentVoxel;
	}

	template <typename VoxelType>
	VoxelType LargeVolume<VoxelType>::Sampler::peekVoxel0px0py1pz(void) const
	{
		if( BORDER_HIGH(this->mZPosInVolume) )
		{
			return *(mCurrentVoxel + this->mVolume->m_uBlockSideLength*this->mVolume->m_uBlockSideLength);
		}
		return this->mVolume->getVoxelAt(this->mXPosInVolume,this->mYPosInVolume,this->mZPosInVolume+1);
	}

	template <typename VoxelType>
	VoxelType LargeVolume<VoxelType>::Sampler::peekVoxel0px1py1nz(void) const
	{
		if( BORDER_HIGH(this->mYPosInVolume) && BORDER_LOW(this->mZPosInVolume) )
		{
			return *(mCurrentVoxel + this->mVolume->m_uBlockSideLength - this->mVolume->m_uBlockSideLength*this->mVolume->m_uBlockSideLength);
		}
		return this->mVolume->getVoxelAt(this->mXPosInVolume,this->mYPosInVolume+1,this->mZPosInVolume-1);
	}

	template <typename VoxelType>
	VoxelType LargeVolume<VoxelType>::Sampler::peekVoxel0px1py0pz(void) const
	{
		if( BORDER_HIGH(this->mYPosInVolume) )
		{
			return *(mCurrentVoxel + this->mVolume->m_uBlockSideLength);
		}
		return this->mVolume->getVoxelAt(this->mXPosInVolume,this->mYPosInVolume+1,this->mZPosInVolume);
	}

	template <typename VoxelType>
	VoxelType LargeVolume<VoxelType>::Sampler::peekVoxel0px1py1pz(void) const
	{
		if( BORDER_HIGH(this->mYPosInVolume) && BORDER_HIGH(this->mZPosInVolume) )
		{
			return *(mCurrentVoxel + this->mVolume->m_uBlockSideLength + this->mVolume->m_uBlockSideLength*this->mVolume->m_uBlockSideLength);
		}
		return this->mVolume->getVoxelAt(this->mXPosInVolume,this->mYPosInVolume+1,this->mZPosInVolume+1);
	}

	//////////////////////////////////////////////////////////////////////////

	template <typename VoxelType>
	VoxelType LargeVolume<VoxelType>::Sampler::peekVoxel1px1ny1nz(void) const
	{
		if( BORDER_HIGH(this->mXPosInVolume) && BORDER_LOW(this->mYPosInVolume) && BORDER_LOW(this->mZPosInVolume) )
		{
			return *(mCurrentVoxel + 1 - this->mVolume->m_uBlockSideLength - this->mVolume->m_uBlockSideLength*this->mVolume->m_uBlockSideLength);
		}
		return this->mVolume->getVoxelAt(this->mXPosInVolume+1,this->mYPosInVolume-1,this->mZPosInVolume-1);
	}

	template <typename VoxelType>
	VoxelType LargeVolume<VoxelType>::Sampler::peekVoxel1px1ny0pz(void) const
	{
		if( BORDER_HIGH(this->mXPosInVolume) && BORDER_LOW(this->mYPosInVolume) )
		{
			return *(mCurrentVoxel + 1 - this->mVolume->m_uBlockSideLength);
		}
		return this->mVolume->getVoxelAt(this->mXPosInVolume+1,this->mYPosInVolume-1,this->mZPosInVolume);
	}

	template <typename VoxelType>
	VoxelType LargeVolume<VoxelType>::Sampler::peekVoxel1px1ny1pz(void) const
	{
		if( BORDER_HIGH(this->mXPosInVolume) && BORDER_LOW(this->mYPosInVolume) && BORDER_HIGH(this->mZPosInVolume) )
		{
			return *(mCurrentVoxel + 1 - this->mVolume->m_uBlockSideLength + this->mVolume->m_uBlockSideLength*this->mVolume->m_uBlockSideLength);
		}
		return this->mVolume->getVoxelAt(this->mXPosInVolume+1,this->mYPosInVolume-1,this->mZPosInVolume+1);
	}

	template <typename VoxelType>
	VoxelType LargeVolume<VoxelType>::Sampler::peekVoxel1px0py1nz(void) const
	{
		if( BORDER_HIGH(this->mXPosInVolume) && BORDER_LOW(this->mZPosInVolume) )
		{
			return *(mCurrentVoxel + 1 - this->mVolume->m_uBlockSideLength*this->mVolume->m_uBlockSideLength);
		}
		return this->mVolume->getVoxelAt(this->mXPosInVolume+1,this->mYPosInVolume,this->mZPosInVolume-1);
	}

	template <typename VoxelType>
	VoxelType LargeVolume<VoxelType>::Sampler::peekVoxel1px0py0pz(void) const
	{
		if( BORDER_HIGH(this->mXPosInVolume) )
		{
			return *(mCurrentVoxel + 1);
		}
		return this->mVolume->getVoxelAt(this->mXPosInVolume+1,this->mYPosInVolume,this->mZPosInVolume);
	}

	template <typename VoxelType>
	VoxelType LargeVolume<VoxelType>::Sampler::peekVoxel1px0py1pz(void) const
	{
		if( BORDER_HIGH(this->mXPosInVolume) && BORDER_HIGH(this->mZPosInVolume) )
		{
			return *(mCurrentVoxel + 1 + this->mVolume->m_uBlockSideLength*this->mVolume->m_uBlockSideLength);
		}
		return this->mVolume->getVoxelAt(this->mXPosInVolume+1,this->mYPosInVolume,this->mZPosInVolume+1);
	}

	template <typename VoxelType>
	VoxelType LargeVolume<VoxelType>::Sampler::peekVoxel1px1py1nz(void) const
	{
		if( BORDER_HIGH(this->mXPosInVolume) && BORDER_HIGH(this->mYPosInVolume) && BORDER_LOW(this->mZPosInVolume) )
		{
			return *(mCurrentVoxel + 1 + this->mVolume->m_uBlockSideLength - this->mVolume->m_uBlockSideLength*this->mVolume->m_uBlockSideLength);
		}
		return this->mVolume->getVoxelAt(this->mXPosInVolume+1,this->mYPosInVolume+1,this->mZPosInVolume-1);
	}

	template <typename VoxelType>
	VoxelType LargeVolume<VoxelType>::Sampler::peekVoxel1px1py0pz(void) const
	{
		if( BORDER_HIGH(this->mXPosInVolume) && BORDER_HIGH(this->mYPosInVolume) )
		{
			return *(mCurrentVoxel + 1 + this->mVolume->m_uBlockSideLength);
		}
		return this->mVolume->getVoxelAt(this->mXPosInVolume+1,this->mYPosInVolume+1,this->mZPosInVolume);
	}

	template <typename VoxelType>
	VoxelType LargeVolume<VoxelType>::Sampler::peekVoxel1px1py1pz(void) const
	{
		if( BORDER_HIGH(this->mXPosInVolume) && BORDER_HIGH(this->mYPosInVolume) && BORDER_HIGH(this->mZPosInVolume) )
		{
			return *(mCurrentVoxel + 1 + this->mVolume->m_uBlockSideLength + this->mVolume->m_uBlockSideLength*this->mVolume->m_uBlockSideLength);
		}
		return this->mVolume->getVoxelAt(this->mXPosInVolume+1,this->mYPosInVolume+1,this->mZPosInVolume+1);
	}
}

#undef BORDER_LOW
#undef BORDER_HIGH
