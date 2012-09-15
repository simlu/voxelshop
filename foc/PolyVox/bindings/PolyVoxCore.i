%module PolyVoxCore

#define POLYVOX_API

//This macro allows us to use Python properties on our classes
%define PROPERTY(type,name,getter,setter)
%extend type {
	%pythoncode %{
		__swig_getmethods__["name"] = getter
		__swig_setmethods__["name"] = setter
		if _newclass: name = property(getter, setter)
	%}
};
%enddef

//Put this in an %extend section to wrap operator<< as __str__
%define STR()
const char* __str__() {
	std::ostringstream out;
	out << *$self;
	return out.str().c_str();
}
%enddef

%feature("autodoc", "1");

%include "stdint.i"
%include "std_vector.i"
%include "Vector.i"
%include "DefaultMarchingCubesController.i"
%include "Density.i"
%include "Material.i"
%include "MaterialDensityPair.i"
%include "Region.i"
%include "SimpleVolume.i"
//%include "TypeDef.i"
//%include "SubArray.i"
//%include "Array.i"
%include "VertexTypes.i"
%include "SurfaceMesh.i"
//%include "SimpleVolumeSampler.i"
%include "MarchingCubesSurfaceExtractor.i"
//%include "CubicSurfaceExtractor.i"
//%include "CubicSurfaceExtractorWithNormals.i"
//%include "MeshDecimator.i"