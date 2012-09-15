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
	////////////////////////////////////////////////////////////////////////////////
	/// Creates an empty array with no elements. You will have to call resize() on this
	/// array before it can be used.
	////////////////////////////////////////////////////////////////////////////////
	template <uint32_t noOfDims, typename ElementType>
	Array<noOfDims, ElementType>::Array()
		:m_pDimensions(0)
		,m_pOffsets(0)
		,m_uNoOfElements(0)
		,m_pElements(0)
	{
	}

	////////////////////////////////////////////////////////////////////////////////
	/// Creates an array with the specified dimensions.
	/// \param pDimensions The dimensions of the array. You can also use the ArraySizes
	/// class to construct this more easily.
	/// \sa ArraySizes
	////////////////////////////////////////////////////////////////////////////////
	template <uint32_t noOfDims, typename ElementType>
	Array<noOfDims, ElementType>::Array(const uint32_t (&pDimensions)[noOfDims])
		:m_pDimensions(0)
		,m_pOffsets(0)
		,m_uNoOfElements(0)
		,m_pElements(0)
	{
		resize(pDimensions);
	}

	////////////////////////////////////////////////////////////////////////////////
	/// Destroys the array and releases all owned memory.
	////////////////////////////////////////////////////////////////////////////////
	template <uint32_t noOfDims, typename ElementType>
	Array<noOfDims, ElementType>::~Array()
	{
		deallocate();
	}

	////////////////////////////////////////////////////////////////////////////////
	/// An N-dimensional array can be conceptually consists of N subarrays each of which
	/// has N-1 dimensions. For example, a 3D array conceptually consists of three 2D
	/// arrays. This operator is used to access the subarray at the specified index.
	/// Crucially, the subarray defines a similar operator allowing them to be chained
	/// together to convieniently access a particular element.
	/// \param uIndex The zero-based index of the subarray to retrieve.
	/// \return The requested SubArray
	////////////////////////////////////////////////////////////////////////////////
	template <uint32_t noOfDims, typename ElementType>
	SubArray<noOfDims-1, ElementType> Array<noOfDims, ElementType>::operator[](uint32_t uIndex)
	{
		assert(uIndex<m_pDimensions[0]);
		return
			SubArray<noOfDims-1, ElementType>(&m_pElements[uIndex*m_pOffsets[0]],
			m_pDimensions+1, m_pOffsets+1);
	}

	////////////////////////////////////////////////////////////////////////////////
	/// An N-dimensional array can be conceptually consists of N subarrays each of which
	/// has N-1 dimensions. For example, a 3D array conceptually consists of three 2D
	/// arrays. This operator is used to access the subarray at the specified index.
	/// Crucially, the subarray defines a similar operator allowing them to be chained
	/// together to convieniently access a particular element.
	/// \param uIndex The zero-based index of the subarray to retrieve.
	/// \return The requested SubArray
	////////////////////////////////////////////////////////////////////////////////
	template <uint32_t noOfDims, typename ElementType>
	const SubArray<noOfDims-1, ElementType> Array<noOfDims, ElementType>::operator[](uint32_t uIndex) const
	{
		assert(uIndex<m_pDimensions[0]);
		return
			SubArray<noOfDims-1, ElementType>(&m_pElements[uIndex*m_pOffsets[0]],
			m_pDimensions+1, m_pOffsets+1);
	}

	////////////////////////////////////////////////////////////////////////////////
	/// \return The number of elements in the array.
	/// \sa getRawData()
	////////////////////////////////////////////////////////////////////////////////
	template <uint32_t noOfDims, typename ElementType>
	uint32_t Array<noOfDims, ElementType>::getNoOfElements(void) const
	{
		return m_uNoOfElements;
	}

	////////////////////////////////////////////////////////////////////////////////
	/// Sometimes it is useful to directly manipulate the underlying array without
	/// going through this classes interface. Although this does not honour the principle
	/// of encapsulation it can be done safely if you are careful and can sometimes be
	/// useful. Use getNoOfElements() to determine how far you can safely write.
	/// \return A pointer to the first element of the array
	/// \sa getNoOfElements()
	////////////////////////////////////////////////////////////////////////////////
	template <uint32_t noOfDims, typename ElementType>
	ElementType* Array<noOfDims, ElementType>::getRawData(void) const
	{
		return m_pElements;
	}

	////////////////////////////////////////////////////////////////////////////////
	/// Please note that the existing contents of the array will be lost.
	/// \param pDimensions The new dimensions of the array. You can also use the
	/// ArraySizes class to specify this more easily.
	/// \sa ArraySizes
	////////////////////////////////////////////////////////////////////////////////
	template <uint32_t noOfDims, typename ElementType>
	void Array<noOfDims, ElementType>::resize(const uint32_t (&pDimensions)[noOfDims])
	{
		deallocate();

		m_pDimensions = new uint32_t[noOfDims];
		m_pOffsets = new uint32_t[noOfDims];

		// Calculate all the information you need to use the array
		m_uNoOfElements = 1;
		for (uint32_t i = 0; i<noOfDims; i++)
		{
			assert(pDimensions[i] != 0);

			m_uNoOfElements *= pDimensions[i];
			m_pDimensions[i] = pDimensions[i];
			m_pOffsets[i] = 1;
			for (uint32_t k=noOfDims-1; k>i; k--)
			{
				m_pOffsets[i] *= pDimensions[k];
			}
		}
		// Allocate new elements, let exception propagate
		m_pElements = new ElementType[m_uNoOfElements];
	}

	////////////////////////////////////////////////////////////////////////////////
	/// Because this class does not have a public assignment operator or copy constructor
	/// it cannot be used with the STL swap() function. This function provides an efficient
	/// implementation of that feature.
	/// \param rhs The array to swap this object with.
	////////////////////////////////////////////////////////////////////////////////
	template <uint32_t noOfDims, typename ElementType>
	void Array<noOfDims, ElementType>::swap(Array<noOfDims, ElementType>& rhs)
	{
		//Implement this function without temporary 'Array'
		//objects, as the destructors will free the memory...
		uint32_t* m_pTempDimensions = m_pDimensions;
		uint32_t* m_pTempOffsets = m_pOffsets;
		uint32_t m_uTempNoOfElements = m_uNoOfElements;
		ElementType* m_pTempElements = m_pElements;

		m_pDimensions = rhs.m_pDimensions;
		m_pOffsets = rhs.m_pOffsets;
		m_uNoOfElements = rhs.m_uNoOfElements;
		m_pElements = rhs.m_pElements;

		rhs.m_pDimensions = m_pTempDimensions;
		rhs.m_pOffsets = m_pTempOffsets;
		rhs.m_uNoOfElements = m_uTempNoOfElements;
		rhs.m_pElements = m_pTempElements;
	}

	////////////////////////////////////////////////////////////////////////////////
	/// \param uDimension The dimension to get the size of.
	////////////////////////////////////////////////////////////////////////////////
	template <uint32_t noOfDims, typename ElementType>
	uint32_t Array<noOfDims, ElementType>::getDimension(uint32_t uDimension)
	{
		assert(uDimension < noOfDims);
		return m_pDimensions[uDimension];
	}

	template <uint32_t noOfDims, typename ElementType>
	Array<noOfDims, ElementType>::Array(const Array<noOfDims, ElementType>& rhs)
		:m_pElements(0)
		,m_pDimensions(0)
		,m_pOffsets(0)
		,m_uNoOfElements(0)
	{
		//Not implemented
		assert(false);
	}

	template <uint32_t noOfDims, typename ElementType>
	Array<noOfDims, ElementType>& Array<noOfDims, ElementType>::operator=(const Array<noOfDims, ElementType>& rhs)
	{
		//Not implemented
		assert(false);

		return *this;
	}

	template <uint32_t noOfDims, typename ElementType>
	void Array<noOfDims, ElementType>::deallocate(void)
	{
		delete[] m_pDimensions;
		m_pDimensions = 0;
		delete[] m_pOffsets;
		m_pOffsets = 0;
		delete[] m_pElements;
		m_pElements = 0;

		m_uNoOfElements = 0;
	}

	//****************************************************************************//
	// One dimensional specialisation begins here                                 //
	//****************************************************************************//

	template <typename ElementType>
	Array<1, ElementType>::Array()
		: m_pElements(0)
		,m_pDimensions(0)
	{
	}

	template <typename ElementType>
	Array<1, ElementType>::Array(const uint32_t (&pDimensions)[1])
		: m_pElements(0)
		,m_pDimensions(0)
	{
		resize(pDimensions);
	}

	template <typename ElementType>
	Array<1, ElementType>::~Array()
	{
		deallocate();
	}

	template <typename ElementType>
	ElementType& Array<1, ElementType>::operator[] (uint32_t uIndex)
	{
		assert(uIndex<m_pDimensions[0]);
		return m_pElements[uIndex];
	}

	template <typename ElementType>
	const ElementType& Array<1, ElementType>::operator[] (uint32_t uIndex) const
	{
		assert(uIndex<m_pDimensions[0]);
		return m_pElements[uIndex];
	}

	template <typename ElementType>
	uint32_t Array<1, ElementType>::getNoOfElements(void) const
	{
		return m_pDimensions[0];
	}

	template <typename ElementType>
	ElementType* Array<1, ElementType>::getRawData(void) const
	{
		return m_pElements;
	}

	template <typename ElementType>
	void Array<1, ElementType>::resize(const uint32_t (&pDimensions)[1])
	{
		deallocate();

		m_pDimensions = new uint32_t[1];
		m_pDimensions[0] = pDimensions[0];

		// Allocate new elements, let exception propagate
		m_pElements = new ElementType[m_pDimensions[0]];
	}

	template <typename ElementType>
	void Array<1, ElementType>::swap(Array<1, ElementType>& rhs)
	{
		//Implement this function without temporary 'Array'
		//objects, as the destructors will free the memory...
		uint32_t* m_pTempDimensions = m_pDimensions;
		ElementType* m_pTempElements = m_pElements;

		m_pDimensions = rhs.m_pDimensions;
		m_pElements = rhs.m_pElements;

		rhs.m_pDimensions = m_pTempDimensions;
		rhs.m_pElements = m_pTempElements;
	}

	template <typename ElementType>
	Array<1, ElementType>::Array(const Array<1, ElementType>& rhs)
		: m_pElements(0)
		,m_pDimensions(0)
	{
		//Not implemented
		assert(false);
	}

	template <typename ElementType>
	Array<1, ElementType>& Array<1, ElementType>::operator=(const Array<1, ElementType>& rhs)
	{
		//Not implemented
		assert(false);

		return *this;
	}

	template <typename ElementType>
	void Array<1, ElementType>::deallocate(void)
	{
		delete[] m_pDimensions;
		m_pDimensions = 0;
		delete[] m_pElements;
		m_pElements = 0;
	}
}//namespace PolyVox
