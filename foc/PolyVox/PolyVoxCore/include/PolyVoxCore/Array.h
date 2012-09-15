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

#ifndef __PolyVox_Array_H__
#define __PolyVox_Array_H__

#include "PolyVoxImpl/SubArray.h"

#include "PolyVoxCore/ArraySizes.h" //Not strictly required, but convienient

namespace PolyVox
{
	///Provides an efficient implementation of a multidimensional array.
	////////////////////////////////////////////////////////////////////////////////
	/// While C++ provides one-dimensional arrays as a language feature, it does not
	/// provide a simple and intuitive way of working with multidimensional arrays
	/// whose sizes are specified at runtime. Such a construct is very useful within
	/// the context of PolyVox, and this Array class provides such functionality
	/// implemented via templates and partial specialisation.
	///
	/// The following code snippet illustrates the basic usage of the class by writing
	/// a different value into each element:
	///
	/// \code
	/// int width = 5;
	/// int height = 10;
	/// int depth = 20;
	///
	/// //Creates a 3D array of integers with dimensions 5x10x20
	/// Array<3, int> myArray(ArraySizes(width)(height)(depth));
	///
	/// int ct = 1;
	/// for(int z = 0; z < depth; z++)
	/// {
	/// 	for(int y = 0; y < height; y++)
	/// 	{
	/// 		for(int x = 0; x < width; x++)
	/// 		{
	/// 			myArray[x][y][z] = ct;
	/// 			ct++;
	/// 		}
	/// 	}
	/// }
	/// \endcode
	///
	/// Although the constructor and resize() functions both take the required dimensions
	/// as an array of ints, note that the ArraySizes class can be used to build this
	/// inline. This is a more convienient way of specifying these dimensions.
	///
	/// Note also that this class has a private assignment operator and copy constructor
	/// in order to prevent copying. This is because a deep copy is a potentially slow
	/// operation and can often be performed inadvertently by functions such as std::swap,
	/// while a shallow copy introduces confusion over memory ownership.
	////////////////////////////////////////////////////////////////////////////////
	template <uint32_t noOfDims, typename ElementType>
	class Array
	{
	public:
		///Constructor
		Array<noOfDims, ElementType>();
		///Constructor
		Array<noOfDims, ElementType>(const uint32_t (&pDimensions)[noOfDims]);
		///Destructor
		~Array<noOfDims, ElementType>();

		///Subarray access
		SubArray<noOfDims-1, ElementType> operator[](uint32_t uIndex);
		///Subarray access
		const SubArray<noOfDims-1, ElementType> operator[](uint32_t uIndex) const;

		///Gets the total number of elements in this array
		uint32_t getNoOfElements(void) const;
		///Gets a pointer to the first element of the array
		ElementType* getRawData(void) const;

		///Resize the array to the specified dimensions
		void resize(const uint32_t (&pDimensions)[noOfDims]);
		///Swaps the contents of this array with the one specified
		void swap(Array<noOfDims, ElementType>& rhs);
		///Get the size of the Array along the specified dimension
		uint32_t getDimension(uint32_t uDimension);

	private:
		Array<noOfDims, ElementType>(const Array<noOfDims, ElementType>& rhs);

		Array<noOfDims, ElementType>& operator=(const Array<noOfDims, ElementType>& rhs);

		void deallocate(void);

		uint32_t * m_pDimensions;
		uint32_t * m_pOffsets;
		uint32_t m_uNoOfElements;
		ElementType * m_pElements;
	};

	template <typename ElementType>
	class Array<1, ElementType>
	{
	public:
		Array<1, ElementType>();

		Array<1, ElementType>(const uint32_t (&pDimensions)[1]);

		~Array<1, ElementType>();

		ElementType& operator[] (uint32_t uIndex);

		const ElementType& operator[] (uint32_t uIndex) const;

		uint32_t getNoOfElements(void) const;

		ElementType* getRawData(void) const;

		void resize(const uint32_t (&pDimensions)[1]);

		void swap(Array<1, ElementType>& rhs);

	private:
		Array<1, ElementType>(const Array<1, ElementType>& rhs);

		Array<1, ElementType>& operator=(const Array<1, ElementType>& rhs);

		void deallocate(void);

		uint32_t * m_pDimensions;
		ElementType * m_pElements;
	};

	template <typename ElementType>
	class Array<0, ElementType>
	{
		//Zero dimensional array is meaningless.
	};

	//Some handy typedefs
	///A 1D Array of floats.
	typedef Array<1,float> Array1DFloat;
	///A 1D Array of doubles.
	typedef Array<1,double> Array1DDouble;
	///A 1D Array of signed 8-bit values.
	typedef Array<1,int8_t> Array1DInt8;
	///A 1D Array of unsigned 8-bit values.
	typedef Array<1,uint8_t> Array1DUint8;
	///A 1D Array of signed 16-bit values.
	typedef Array<1,int16_t> Array1DInt16;
	///A 1D Array of unsigned 16-bit values.
	typedef Array<1,uint16_t> Array1DUint16;
	///A 1D Array of signed 32-bit values.
	typedef Array<1,int32_t> Array1DInt32;
	///A 1D Array of unsigned 32-bit values.
	typedef Array<1,uint32_t> Array1DUint32;

	///A 2D Array of floats.
	typedef Array<2,float> Array2DFloat;
	///A 2D Array of doubles.
    typedef Array<2,double> Array2DDouble;
	///A 2D Array of signed 8-bit values.
	typedef Array<2,int8_t> Array2DInt8;
	///A 2D Array of unsigned 8-bit values.
	typedef Array<2,uint8_t> Array2DUint8;
	///A 2D Array of signed 16-bit values.
	typedef Array<2,int16_t> Array2DInt16;
	///A 2D Array of unsigned 16-bit values.
	typedef Array<2,uint16_t> Array2DUint16;
	///A 2D Array of signed 32-bit values.
	typedef Array<2,int32_t> Array2DInt32;
	///A 2D Array of unsigned 32-bit values.
	typedef Array<2,uint32_t> Array2DUint32;

	///A 3D Array of floats.
	typedef Array<3,float> Array3DFloat;
	///A 3D Array of doubles.
    typedef Array<3,double> Array3DDouble;
	///A 3D Array of signed 8-bit values.
	typedef Array<3,int8_t> Array3DInt8;
	///A 3D Array of unsigned 8-bit values.
	typedef Array<3,uint8_t> Array3DUint8;
	///A 3D Array of signed 16-bit values.
	typedef Array<3,int16_t> Array3DInt16;
	///A 3D Array of unsigned 16-bit values.
	typedef Array<3,uint16_t> Array3DUint16;
	///A 3D Array of signed 32-bit values.
	typedef Array<3,int32_t> Array3DInt32;
	///A 3D Array of unsigned 32-bit values.
	typedef Array<3,uint32_t> Array3DUint32;
}//namespace PolyVox

#include "PolyVoxCore/Array.inl"

#endif
