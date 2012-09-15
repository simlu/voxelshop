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

#ifndef __PolyVox_SurfaceVertex_H__
#define __PolyVox_SurfaceVertex_H__

#include "PolyVoxImpl/TypeDef.h"

#include "PolyVoxCore/Vector.h"

#include <bitset>
#include <vector>

namespace PolyVox
{	
#ifdef SWIG
	class PositionMaterial
#else
	class POLYVOX_API PositionMaterial
#endif
	{
	public:	
		PositionMaterial();
		PositionMaterial(Vector3DFloat positionToSet, float materialToSet);

		float getMaterial(void) const;
		const Vector3DFloat& getPosition(void) const;

		void setMaterial(float materialToSet);
		void setPosition(const Vector3DFloat& positionToSet);
	public:		
		//Nicely fits into four floats.
		Vector3DFloat position;
		float material;
	};	

#ifdef SWIG
	class PositionMaterialNormal
#else
	class POLYVOX_API PositionMaterialNormal
#endif
	{
	public:	
		PositionMaterialNormal();
		PositionMaterialNormal(Vector3DFloat positionToSet, float materialToSet);
		PositionMaterialNormal(Vector3DFloat positionToSet, Vector3DFloat normalToSet, float materialToSet);	

		float getMaterial(void) const;
		const Vector3DFloat& getNormal(void) const;
		const Vector3DFloat& getPosition(void) const;	

		void setMaterial(float materialToSet);
		void setNormal(const Vector3DFloat& normalToSet);
		void setPosition(const Vector3DFloat& positionToSet);

	public:		
		//Nicely fits into seven floats, meaning we
		//can squeeze in one more for material blending.
		Vector3DFloat position;
		Vector3DFloat normal;
		float material; //FIXME: This shouldn't be float on CPU?
	};
}

#endif
