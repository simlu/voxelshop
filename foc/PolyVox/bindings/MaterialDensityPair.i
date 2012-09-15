%module MaterialDensityPair
%{
#include "MaterialDensityPair.h"
%}

%include "MaterialDensityPair.h"

%template(MaterialDensityPair44) PolyVox::MaterialDensityPair<uint8_t, 4, 4>;
%template(MaterialDensityPair88) PolyVox::MaterialDensityPair<uint16_t, 8, 8>;
