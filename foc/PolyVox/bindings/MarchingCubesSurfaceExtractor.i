%module MarchingCubesSurfaceExtractor
%{
#include "SimpleVolume.h"
#include "Material.h"
#include "MarchingCubesSurfaceExtractor.h"
%}

%include "SimpleVolume.h"
%include "MarchingCubesSurfaceExtractor.h"

%template(SurfaceExtractorSimpleVolumeDensity8) PolyVox::MarchingCubesSurfaceExtractor<PolyVox::SimpleVolume<PolyVox::Density8> >;
