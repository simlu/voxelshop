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
	template<typename VolumeType>
	RaycastWithCallback<VolumeType>::RaycastWithCallback(VolumeType* volData, const Vector3DFloat& v3dStart, const Vector3DFloat& v3dDirectionAndLength, polyvox_function<bool(const Vector3DInt32& position)> funcCallback)
		:m_volData(volData)
		,m_sampVolume(volData)
		,m_v3dStart(v3dStart)
		,m_v3dDirectionAndLength(v3dDirectionAndLength)
		,m_funcCallback(funcCallback)
	{
		//Check the user provided a callback, because it
		//is used to determine when to finish the raycast.
		assert(m_funcCallback);
	}

	template<typename VolumeType>
	void RaycastWithCallback<VolumeType>::setStart(const Vector3DFloat& v3dStart)
	{
		m_v3dStart = v3dStart;
	}

	template<typename VolumeType>
	void RaycastWithCallback<VolumeType>::setDirection(const Vector3DFloat& v3dDirectionAndLength)
	{
		m_v3dDirectionAndLength = v3dDirectionAndLength;
	}

	template<typename VolumeType>
	void RaycastWithCallback<VolumeType>::execute(void)
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
	void RaycastWithCallback<VolumeType>::doRaycast(float x1, float y1, float z1, float x2, float y2, float z2)
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

		for(;;)
		{
			//Call the callback. If it returns false then finish the loop.
			if(!m_funcCallback(Vector3DInt32(i,j,k)))
			{
				break;
			}

			if(tx <= ty && tx <= tz)
			{
				tx += deltatx;
				i += di;

				if(di == 1) m_sampVolume.movePositiveX();
				if(di == -1) m_sampVolume.moveNegativeX();
			} else if (ty <= tz)
			{
				ty += deltaty;
				j += dj;

				if(dj == 1) m_sampVolume.movePositiveY();
				if(dj == -1) m_sampVolume.moveNegativeY();
			} else 
			{
				tz += deltatz;
				k += dk;

				if(dk == 1) m_sampVolume.movePositiveZ();
				if(dk == -1) m_sampVolume.moveNegativeZ();
			}
		}
	}
}