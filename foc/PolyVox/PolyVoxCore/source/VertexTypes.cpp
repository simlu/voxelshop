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

#include "PolyVoxCore/VertexTypes.h"

namespace PolyVox
{
	PositionMaterialNormal::PositionMaterialNormal()
	{
	}

	PositionMaterialNormal::PositionMaterialNormal(Vector3DFloat positionToSet, float materialToSet)
		:position(positionToSet)
		,material(materialToSet)
	{
		
	}

	PositionMaterialNormal::PositionMaterialNormal(Vector3DFloat positionToSet, Vector3DFloat normalToSet, float materialToSet)
		:position(positionToSet)
		,normal(normalToSet)
		,material(materialToSet)
	{
	}

	float PositionMaterialNormal::getMaterial(void) const
	{
		return material;
	}

	const Vector3DFloat& PositionMaterialNormal::getNormal(void) const
	{
		return normal;
	}

	const Vector3DFloat& PositionMaterialNormal::getPosition(void) const
	{
		return position;
	}

	void PositionMaterialNormal::setMaterial(float materialToSet)
	{
		material = materialToSet;
	}

	void PositionMaterialNormal::setNormal(const Vector3DFloat& normalToSet)
	{
		normal = normalToSet;
	}	

	void PositionMaterialNormal::setPosition(const Vector3DFloat& positionToSet)
	{
		position = positionToSet;
	}

	////////////////////////////////////////////////////////////////////////////////
	// PositionMaterial
	////////////////////////////////////////////////////////////////////////////////

	PositionMaterial::PositionMaterial()
	{
	}

	PositionMaterial::PositionMaterial(Vector3DFloat positionToSet, float materialToSet)
		:position(positionToSet)
		,material(materialToSet)
	{
		
	}

	float PositionMaterial::getMaterial(void) const
	{
		return material;
	}

	const Vector3DFloat& PositionMaterial::getPosition(void) const
	{
		return position;
	}

	void PositionMaterial::setMaterial(float materialToSet)
	{
		material = materialToSet;
	}

	void PositionMaterial::setPosition(const Vector3DFloat& positionToSet)
	{
		position = positionToSet;
	}
}
