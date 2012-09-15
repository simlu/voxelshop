#include "PolyVoxImpl/TypeDef.h"

namespace PolyVox
{	
	class POLYVOX_API DummyClass
	{
	public:
		int getx(void);
		int x;
	};
	
	int DummyClass::getx(void)
	{
		return x;
	}
}
