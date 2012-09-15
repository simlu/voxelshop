%module SurfaceMesh
%{
#include "Region.h"
#include "VertexTypes.h"
#include "SurfaceMesh.h"
%}

%include "Region.h"
%include "VertexTypes.h"
%include "SurfaceMesh.h"

//%template(VertexTypeVector) std::vector<PolyVox::VertexType>;
%template(PositionMaterialVector) std::vector<PolyVox::PositionMaterial>;
%template(PositionMaterialNormalVector) std::vector<PolyVox::PositionMaterialNormal>;
%template(LodRecordVector) std::vector<PolyVox::LodRecord>;
%template(uint8Vector) std::vector<uint8_t>;
%template(uint32Vector) std::vector<uint32_t>;

%template(SurfaceMeshPositionMaterial) PolyVox::SurfaceMesh<PolyVox::PositionMaterial>;
%template(SurfaceMeshPositionMaterialNormal) PolyVox::SurfaceMesh<PolyVox::PositionMaterialNormal>;