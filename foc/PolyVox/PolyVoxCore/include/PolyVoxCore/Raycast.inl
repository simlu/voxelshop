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
	/// Builds a Raycast object.
	/// \param volData A pointer to the volume through which the ray will be cast.
	/// \param v3dStart The starting position of the ray.
	/// \param v3dDirectionAndLength The direction of the ray. The length of this vector also
	/// represents the length of the ray.
	/// \param result An instance of RaycastResult in which the result will be stored.
	////////////////////////////////////////////////////////////////////////////////
	template<typename VolumeType>
	Raycast<VolumeType>::Raycast(VolumeType* volData, const Vector3DFloat& v3dStart, const Vector3DFloat& v3dDirectionAndLength, RaycastResult& result, polyvox_function<bool(const typename VolumeType::Sampler& sampler)> funcIsPassable)
		:m_result(result)
		,m_funcIsPassable(funcIsPassable)
		,m_volData(volData)
		,m_sampVolume(volData)
		,m_v3dStart(v3dStart)
		,m_v3dDirectionAndLength(v3dDirectionAndLength)
	{
	}

	////////////////////////////////////////////////////////////////////////////////
	/// \param v3dStart The starting position of the ray.
	////////////////////////////////////////////////////////////////////////////////
	template<typename VolumeType>
	void Raycast<VolumeType>::setStart(const Vector3DFloat& v3dStart)
	{
		m_v3dStart = v3dStart;
	}

	////////////////////////////////////////////////////////////////////////////////
	/// \param v3dDirectionAndLength The direction of the ray. The length of this vector also
	/// represents the length of the ray.
	////////////////////////////////////////////////////////////////////////////////
	template<typename VolumeType>
	void Raycast<VolumeType>::setDirection(const Vector3DFloat& v3dDirectionAndLength)
	{
		//FIXME: We should add a warning when the ray direction is of length one, as this seems to be a common mistake. 
		m_v3dDirectionAndLength = v3dDirectionAndLength;
	}

	////////////////////////////////////////////////////////////////////////////////
	/// The result is stored in the RaycastResult instance which was passed to the constructor.
	////////////////////////////////////////////////////////////////////////////////
	template<typename VolumeType>
	void Raycast<VolumeType>::execute(void)
	{
		//The doRaycast function is assuming that it is iterating over the areas defined between
		//voxels. We actually want to define the areas as being centered on voxels (as this is
		//what the CubicSurfaceExtractor generates). We add (0.5,0.5,0.5) here to adjust for this.
		Vector3DFloat v3dStart = m_v3dStart + Vector3DFloat(0.5f, 0.5f, 0.5f);

		//Compute the end point
		Vector3DFloat v3dEnd = v3dStart + m_v3dDirectionAndLength;

		//Do the raycast
		doRaycast(v3dStart.getX(), v3dStart.getY(), v3dStart.getZ(), v3dEnd.getX(), v3dEnd.getY(), v3dEnd.getZ());
	}

	// This function is based on Christer Ericson's code and description of the 'Uniform Grid Intersection Test' in
	// 'Real Time Collision Detection'. The following information from the errata on the book website is also relevent:
	//
	//	pages 326-327. In the function VisitCellsOverlapped() the two lines calculating tx and ty are incorrect.
	//  The less-than sign in each line should be a greater-than sign. That is, the two lines should read:
	//
	//	float tx = ((x1 > x2) ? (x1 - minx) : (maxx - x1)) / Abs(x2 - x1);
	//	float ty = ((y1 > y2) ? (y1 - miny) : (maxy - y1)) / Abs(y2 - y1);
	//
	//	Thanks to Jetro Lauha of Fathammer in Helsinki, Finland for reporting this error.
	//
	//	Jetro also points out that the computations of i, j, iend, and jend are incorrectly rounded if the line
	//  coordinates are allowed to go negative. While that was not really the intent of the code — that is, I
	//  assumed grids to be numbered from (0, 0) to (m, n) — I'm at fault for not making my assumption clear.
	//  Where it is important to handle negative line coordinates the computation of these variables should be
	//  changed to something like this:
	//
	//	// Determine start grid cell coordinates (i, j)
	//	int i = (int)floorf(x1 / CELL_SIDE);
	//	int j = (int)floorf(y1 / CELL_SIDE);
	//
	//	// Determine end grid cell coordinates (iend, jend)
	//	int iend = (int)floorf(x2 / CELL_SIDE);
	//	int jend = (int)floorf(y2 / CELL_SIDE);
	//
	//	page 328. The if-statement that reads "if (ty <= tx && ty <= tz)" has a superfluous condition.
	//  It should simply read "if (ty <= tz)".
	//
	//	This error was reported by Joey Hammer (PixelActive). 
	template<typename VolumeType>
	void Raycast<VolumeType>::doRaycast(float x1, float y1, float z1, float x2, float y2, float z2)
	{
		int i = (int)floorf(x1);
		int j = (int)floorf(y1);
		int k = (int)floorf(z1);

		int iend = (int)floorf(x2);
		int jend = (int)floorf(y2);
		int kend = (int)floorf(z2);

		int di = ((x1 < x2) ? 1 : ((x1 > x2) ? -1 : 0));
		int dj = ((y1 < y2) ? 1 : ((y1 > y2) ? -1 : 0));
		int dk = ((z1 < z2) ? 1 : ((z1 > z2) ? -1 : 0));

		float deltatx = 1.0f / std::abs(x2 - x1);
		float deltaty = 1.0f / std::abs(y2 - y1);
		float deltatz = 1.0f / std::abs(z2 - z1);

		float minx = floorf(x1), maxx = minx + 1.0f;
		float tx = ((x1 > x2) ? (x1 - minx) : (maxx - x1)) * deltatx;
		float miny = floorf(y1), maxy = miny + 1.0f;
		float ty = ((y1 > y2) ? (y1 - miny) : (maxy - y1)) * deltaty;
		float minz = floorf(z1), maxz = minz + 1.0f;
		float tz = ((z1 > z2) ? (z1 - minz) : (maxz - z1)) * deltatz;

		m_sampVolume.setPosition(i,j,k);
		m_result.previousVoxel = Vector3DInt32(i,j,k);

		for(;;)
		{
			if(!m_funcIsPassable(m_sampVolume))
			{
				m_result.foundIntersection = true;
				m_result.intersectionVoxel = Vector3DInt32(i,j,k);
				return;
			}
			m_result.previousVoxel = Vector3DInt32(i,j,k);

			if(tx <= ty && tx <= tz)
			{
				if(i == iend) break;
				tx += deltatx;
				i += di;

				if(di == 1) m_sampVolume.movePositiveX();
				if(di == -1) m_sampVolume.moveNegativeX();
			} else if (ty <= tz)
			{
				if(j == jend) break;
				ty += deltaty;
				j += dj;

				if(dj == 1) m_sampVolume.movePositiveY();
				if(dj == -1) m_sampVolume.moveNegativeY();
			} else 
			{
				if(k == kend) break;
				tz += deltatz;
				k += dk;

				if(dk == 1) m_sampVolume.movePositiveZ();
				if(dk == -1) m_sampVolume.moveNegativeZ();
			}
		}

		//Didn't hit anything
		m_result.foundIntersection = false;
		m_result.intersectionVoxel = Vector3DInt32(0,0,0);
		m_result.previousVoxel = Vector3DInt32(0,0,0);
	}
}
