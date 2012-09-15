%module SimpleVolume
%{
#include "Material.h"
#include "Density.h"
#include "SimpleVolume.h"
%}

%import "BaseVolume.h"
%include "Material.h"
%include "Density.h"
%include "SimpleVolume.h"

%template(BaseVolumeDensity8) PolyVox::BaseVolume<PolyVox::Density8>;
%template(SimpleVolumeDensity8) PolyVox::SimpleVolume<PolyVox::Density8>;

%template(BaseVolumeMaterial8) PolyVox::BaseVolume<PolyVox::Material8>;
%template(SimpleVolumeMaterial8) PolyVox::SimpleVolume<PolyVox::Material8>;

%template(BaseVolumeMaterial16) PolyVox::BaseVolume<PolyVox::Material16>;
%template(SimpleVolumeMaterial16) PolyVox::SimpleVolume<PolyVox::Material16>;

%template(BaseVolumeMaterialDensityPair44) PolyVox::BaseVolume<PolyVox::MaterialDensityPair44>;
%template(SimpleVolumeMaterialDensityPair44) PolyVox::SimpleVolume<PolyVox::MaterialDensityPair44>;

%template(BaseVolumeMaterialDensityPair88) PolyVox::BaseVolume<PolyVox::MaterialDensityPair88>;
%template(SimpleVolumeMaterialDensityPair88) PolyVox::SimpleVolume<PolyVox::MaterialDensityPair88>;