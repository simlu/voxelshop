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

#ifndef __PolyVox_MaterialDensityPair_H__
#define __PolyVox_MaterialDensityPair_H__

#include "PolyVoxCore/DefaultIsQuadNeeded.h" //we'll specialise this function for this voxel type
#include "PolyVoxCore/DefaultMarchingCubesController.h" //We'll specialise the controller contained in here

#include "PolyVoxImpl/TypeDef.h"

namespace PolyVox
{
	/// This class represents a voxel storing only a density.
	////////////////////////////////////////////////////////////////////////////////
	/// In order to perform a surface extraction on a LargeVolume, PolyVox needs the underlying
	/// voxel type to provide both getDensity() and getMaterial() functions. The getDensity()
	/// function is used to determine if a voxel is 'solid', and if it is then the getMaterial()
	/// funtion is used to determine what material should be assigned to the resulting mesh.
	///
	/// This class meets these requirements, and does so by storing and returning both a material
	/// and a density value. Via the template parameters it is possible to control how much
	/// precision is given to each. For example, if you create a class with 8 bits of storage,
	/// you might choose to allocate 6 bits for the density and 2 bits for the material.
	///
	/// \sa Density, Material
	////////////////////////////////////////////////////////////////////////////////
	template <typename Type, uint8_t NoOfMaterialBits, uint8_t NoOfDensityBits>
	class MaterialDensityPair
	{
	public:
		//We expose DensityType and MaterialType in this way so that, when code is
		//templatised on voxel type, it can determine the underlying storage type
		//using code such as 'VoxelType::DensityType value = voxel.getDensity()'
		//or 'VoxelType::MaterialType value = voxel.getMaterial()'.
		typedef Type DensityType;
		typedef Type MaterialType;

		MaterialDensityPair() : m_uMaterial(0), m_uDensity(0) {}
		MaterialDensityPair(Type uMaterial, Type uDensity) : m_uMaterial(uMaterial), m_uDensity(uDensity) {}

		bool operator==(const MaterialDensityPair& rhs) const throw()
		{
			return (m_uMaterial == rhs.m_uMaterial) && (m_uDensity == rhs.m_uDensity);
		};

		bool operator!=(const MaterialDensityPair& rhs) const throw()
		{
			return !(*this == rhs);
		}

		MaterialDensityPair<Type, NoOfMaterialBits, NoOfDensityBits>& operator+=(const MaterialDensityPair<Type, NoOfMaterialBits, NoOfDensityBits>& rhs)
		{
			m_uDensity += rhs.m_uDensity;

			// What should we do with the material? Conceptually the idea of adding materials makes no sense, but for our
			// purposes we consider the 'sum' of two materials to just be the max. At least this way it is commutative.
			m_uMaterial = (std::max)(m_uMaterial, rhs.m_uMaterial);

			return *this;
		}

		MaterialDensityPair<Type, NoOfMaterialBits, NoOfDensityBits>& operator/=(uint32_t rhs)
		{
			// There's nothing sensible we can do with the material, so this function only affects the density.
			m_uDensity /= rhs;
			return *this;
		}

		DensityType getDensity() const throw() { return m_uDensity; }
		MaterialType getMaterial() const throw() { return m_uMaterial; }

		void setDensity(DensityType uDensity) { m_uDensity = uDensity; }
		void setMaterial(MaterialType uMaterial) { m_uMaterial = uMaterial; }

		static DensityType getMaxDensity() throw() { return (0x01 << NoOfDensityBits) - 1; }
		static DensityType getMinDensity() throw() { return 0; }

	private:
		MaterialType m_uMaterial : NoOfMaterialBits;
		DensityType m_uDensity : NoOfDensityBits;
	};

	template<typename Type, uint8_t NoOfMaterialBits, uint8_t NoOfDensityBits>
	class DefaultIsQuadNeeded< MaterialDensityPair<Type, NoOfMaterialBits, NoOfDensityBits> >
	{
	public:
		bool operator()(MaterialDensityPair<Type, NoOfMaterialBits, NoOfDensityBits> back, MaterialDensityPair<Type, NoOfMaterialBits, NoOfDensityBits> front, float& materialToUse)
		{
			if((back.getMaterial() > 0) && (front.getMaterial() == 0))
			{
				materialToUse = static_cast<float>(back.getMaterial());
				return true;
			}
			else
			{
				return false;
			}
		}
	};

	template <typename Type, uint8_t NoOfMaterialBits, uint8_t NoOfDensityBits>
	class DefaultMarchingCubesController< MaterialDensityPair<Type, NoOfMaterialBits, NoOfDensityBits> >
	{
	public:
		typedef Type DensityType;
		typedef Type MaterialType;

		DefaultMarchingCubesController(void)
		{
			// Default to a threshold value halfway between the min and max possible values.
			m_tThreshold = (MaterialDensityPair<Type, NoOfMaterialBits, NoOfDensityBits>::getMinDensity() + MaterialDensityPair<Type, NoOfMaterialBits, NoOfDensityBits>::getMaxDensity()) / 2;
		}

		DefaultMarchingCubesController(DensityType tThreshold)
		{
			m_tThreshold = tThreshold;
		}

		DensityType convertToDensity(MaterialDensityPair<Type, NoOfMaterialBits, NoOfDensityBits> voxel)
		{
			return voxel.getDensity();
		}

		MaterialType convertToMaterial(MaterialDensityPair<Type, NoOfMaterialBits, NoOfDensityBits> voxel)
		{
			return voxel.getMaterial();
		}

		DensityType getThreshold(void)
		{			
			return m_tThreshold;
		}		

	private:
		DensityType m_tThreshold;
	};

	typedef MaterialDensityPair<uint8_t, 4, 4> MaterialDensityPair44;
	typedef MaterialDensityPair<uint16_t, 8, 8> MaterialDensityPair88;
}

#endif
