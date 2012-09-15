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
	template <typename IteratorType>
	void IteratorController<IteratorType>::reset(void)
	{
		m_Iter->setPosition(m_regValid.getLowerCorner());
	}

	template <typename IteratorType>
	bool IteratorController<IteratorType>::moveForward(void)
	{
		Vector3DInt32 v3dInitialPosition(m_Iter->getPosition().getX(), m_Iter->getPosition().getY(), m_Iter->getPosition().getZ());

		if(v3dInitialPosition.getX() < m_regValid.getUpperCorner().getX())
		{
			m_Iter->movePositiveX();
			return true;
		}

		v3dInitialPosition.setX(m_regValid.getLowerCorner().getX());

		if(v3dInitialPosition.getY() < m_regValid.getUpperCorner().getY())
		{
			v3dInitialPosition.setY(v3dInitialPosition.getY() + 1);
			m_Iter->setPosition(v3dInitialPosition);
			return true;
		}

		v3dInitialPosition.setY(m_regValid.getLowerCorner().getY());

		if(v3dInitialPosition.getZ() < m_regValid.getUpperCorner().getZ())
		{
			v3dInitialPosition.setZ(v3dInitialPosition.getZ() + 1);
			m_Iter->setPosition(v3dInitialPosition);
			return true;
		}

		return false;
	}
}
