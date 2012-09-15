%module CubicSurfaceExtractor
%{
#include "SimpleVolume.h"
#include "Array.h"
#include "CubicSurfaceExtractor.h"
%}

%include "SimpleVolume.h"
%include "Array.h"
%include "CubicSurfaceExtractor.h"

%template(CubicSurfaceExtractorMaterial8) PolyVox::CubicSurfaceExtractor<PolyVox::Material8>;
%template(CubicSurfaceExtractorDensity8) PolyVox::CubicSurfaceExtractor<PolyVox::Density8>;