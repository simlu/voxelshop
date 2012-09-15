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

#ifndef __PolyVox_Utility_H__
#define __PolyVox_Utility_H__

#include "PolyVoxImpl/TypeDef.h"

#include <cassert>

namespace PolyVox
{
	POLYVOX_API uint8_t logBase2(uint32_t uInput);
	POLYVOX_API bool isPowerOf2(uint32_t uInput);

	template <typename Type>
        Type trilinearlyInterpolate(
        const Type& v000,const Type& v100,const Type& v010,const Type& v110,
        const Type& v001,const Type& v101,const Type& v011,const Type& v111,
        const float x, const float y, const float z)
    {
        assert((x >= 0.0f) && (y >= 0.0f) && (z >= 0.0f) && 
            (x <= 1.0f) && (y <= 1.0f) && (z <= 1.0f));

		//Interpolate along X
		Type v000_v100 = (v100 - v000) * x + v000;
		Type v001_v101 = (v101 - v001) * x + v001;
		Type v010_v110 = (v110 - v010) * x + v010;
		Type v011_v111 = (v111 - v011) * x + v011;

		//Interpolate along Y
		Type v000_v100__v010_v110 = (v010_v110 - v000_v100) * y + v000_v100;
		Type v001_v101__v011_v111 = (v011_v111 - v001_v101) * y + v001_v101;

		//Interpolate along Z
		Type v000_v100__v010_v110____v001_v101__v011_v111 = (v001_v101__v011_v111 - v000_v100__v010_v110) * z + v000_v100__v010_v110;

		return v000_v100__v010_v110____v001_v101__v011_v111;
    }
}

#endif
