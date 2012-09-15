%module Material
%{
#include "Material.h"
%}

%include "Material.h"

%template(Material8) PolyVox::Material<uint8_t>;
%template(Material16) PolyVox::Material<uint16_t>;
