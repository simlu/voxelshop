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

#ifndef __PolyVox_MarchingCubesController_H__
#define __PolyVox_MarchingCubesController_H__

#include <limits>

namespace PolyVox
{
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/// This class provides a default implementation of a controller for the MarchingCubesSurfaceExtractor. It controls the behaviour of the
	/// MarchingCubesSurfaceExtractor and provides the required properties from the underlying voxel type.
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/// PolyVox does not enforce any requirements regarding what data must be present in a voxel, and instead allows any primitive or user-defined
	/// type to be used. However, the Marching Cubes algorithm does have some requirents about the underlying data in that conceptually it operates
	/// on a <i>density field</i>. In addition, the PolyVox implementation of the Marching Cubes algorithm also understands the idea of each voxel
	/// having a material which is copied into the vertex data.
	///
	/// Because we want the MarchingCubesSurfaceExtractor to work on <i>any</i> voxel type, we use a <i>Marching Cubes controller</i> (passed as
	/// a parameter of the MarchingCubesSurfaceExtractor) to expose the required properties. This parameter defaults to the DefaultMarchingCubesController.
	/// The main implementation of this class is designed to work with primitives data types, and the class is also specialised for the Material,
	/// Density and MaterialdensityPair classes.
	///
	/// If you create a custom class for your voxel data then you probably want to include a specialisation of DefaultMarchingCubesController,
	/// though you don't have to if you don't want to use the Marching Cubes algorithm or if you prefer to define a seperate Marching Cubes controller
	/// and pass it as an explicit parameter (rather than relying on the default).
	///
	/// For primitive types, the DefaultMarchingCubesController considers the value of the voxel to represent it's density and just returns a constant
	/// for the material. So you can, for example, run the MarchingCubesSurfaceExtractor on a volume of floats or ints.
	///
	/// It is possible to customise the behaviour of the controller by providing a threshold value through the constructor. The extracted surface
	/// will pass through the density value specified by the threshold, and so you should make sure that the threshold value you choose is between
	/// the minimum and maximum values found in your volume data. By default it is in the middle of the representable range of the underlying type.
	///
	/// \sa MarchingCubesSurfaceExtractor
	///
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	template<typename VoxelType>
	class DefaultMarchingCubesController
	{
	public:
		/// Used to inform the MarchingCubesSurfaceExtractor about which type it should use for representing densities.
		typedef VoxelType DensityType;
		/// Used to inform the MarchingCubesSurfaceExtractor about which type it should use for representing materials. We're using a float here
		/// because this implementation always returns a constant value off 1.0f. PolyVox also uses floats to store the materials in the mesh vertices
		/// but this is not really desirable on modern hardware. We'll probably come back to material representation in the future.
		typedef float MaterialType;

		////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		/// Constructor
		////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		/// This version of the constructor takes no parameters and sets the threshold to the middle of the representable range of the underlying type.
		/// For example, if the voxel type is 'uint8_t' then the representable range is 0-255, and the threshold will be set to 127. On the other hand,
		/// if the voxel type is 'float' then the representable range is -FLT_MAX to FLT_MAX and the threshold will be set to zero.
		////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		DefaultMarchingCubesController(void)
		{
			m_tThreshold = ((std::numeric_limits<DensityType>::min)() + (std::numeric_limits<DensityType>::max)()) / 2;
		}

		////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		/// Constructor
		////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		/// This version of the constructor allows you to set a custom threshold.
		/// \param tThreshold The threshold to use.
		////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		DefaultMarchingCubesController(DensityType tThreshold)
		{
			m_tThreshold = tThreshold;
		}

		////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		/// Converts the underlying voxel type into a density value.
		////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		/// The default implementation of this function just returns the voxel type directly and is suitable for primitives types. Specialisations of
		/// this class can modify this behaviour.
		////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		DensityType convertToDensity(VoxelType voxel)
		{
			return voxel;
		}

		////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		/// Converts the underlying voxel type into a material value.
		////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		/// The default implementation of this function just returns the constant '1'. There's not much else it can do, as it needs to work with primitive
		/// types and the actual value of the type is already being considered to be the density. Specialisations of this class can modify this behaviour.
		////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		MaterialType convertToMaterial(VoxelType voxel)
		{
			return 1;
		}

		////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		/// Returns the density value which was passed to the constructor.
		////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		/// As mentioned in the class description, the extracted surface will pass through the density value specified by the threshold, and so you
		/// should make sure that the threshold value you choose is between the minimum and maximum values found in your volume data. By default it
		///is in the middle of the representable range of the underlying type.
		////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		DensityType getThreshold(void)
		{
			return m_tThreshold;
		}

	private:
		DensityType m_tThreshold;
	};
}

#endif
