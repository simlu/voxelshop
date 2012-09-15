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

#ifndef __PolyVox_TypeDef_H__
#define __PolyVox_TypeDef_H__

//Definitions needed to make library functions accessable
// See http://gcc.gnu.org/wiki/Visibility for more info.
#if defined _WIN32 || defined __CYGWIN__
  #define POLYVOX_HELPER_IMPORT __declspec(dllimport)
  #define POLYVOX_HELPER_EXPORT __declspec(dllexport)
  #define POLYVOX_HELPER_LOCAL
#else
  #if __GNUC__ >= 4
    #define POLYVOX_HELPER_IMPORT __attribute__ ((visibility("default")))
    #define POLYVOX_HELPER_EXPORT __attribute__ ((visibility("default")))
    #define POLYVOX_HELPER_LOCAL  __attribute__ ((visibility("hidden")))
  #else
    #define POLYVOX_HELPER_IMPORT
    #define POLYVOX_HELPER_EXPORT
    #define POLYVOX_HELPER_LOCAL
  #endif
#endif

// Now we use the generic helper definitions above to define POLYVOX_API and POLYVOX_LOCAL.
// POLYVOX_API is used for the public API symbols. It either imports or exports (or does nothing for static build)
// POLYVOX_LOCAL is used for non-api symbols.

#ifdef POLYVOX_SHARED // defined if PolyVox is compiled as a shared library
  #ifdef POLYVOX_SHARED_EXPORTS // defined if we are building the PolyVox shared library (instead of using it)
    #define POLYVOX_API POLYVOX_HELPER_EXPORT
  #else
    #define POLYVOX_API POLYVOX_HELPER_IMPORT
  #endif // POLYVOX_SHARED_EXPORTS
  #define POLYVOX_LOCAL POLYVOX_HELPER_LOCAL
#else // POLYVOX_SHARED is not defined: this means PolyVox is a static library.
  #define POLYVOX_API
  #define POLYVOX_LOCAL
#endif // POLYVOX_SHARED

//Check which compiler we are using and work around unsupported features as necessary.
// JJS - Need To Use Boost
#if 1 // defined(_MSC_VER) && (_MSC_VER < 1600) 
	//To support old (pre-vc2010) Microsoft compilers we use boost to replace the
	//std::shared_ptr and potentially other C++0x features. To use this capability you
	//will need to make sure you have boost installed on your system.
	#include <boost/shared_ptr.hpp>
	#define polyvox_shared_ptr boost::shared_ptr

	#include <boost/function.hpp>
	#define polyvox_function boost::function

	#define polyvox_hash std::hash
	// JJS Boost Hash Angry
	//#include <boost/functional/hash.hpp>
	//#define polyvox_hash boost::hash

	#include <boost/bind.hpp>
	#define polyvox_bind boost::bind
	#define polyvox_placeholder_1 _1
	#define polyvox_placeholder_2 _2
	
	#include <boost/static_assert.hpp>
	#define static_assert BOOST_STATIC_ASSERT


	//As long as we're requiring boost, we'll use it to compensate
	//for the missing cstdint header too.

	#include <boost/cstdint.hpp>
	using boost::int8_t;
	using boost::int16_t;
	using boost::int32_t;
	using boost::uint8_t;
	using boost::uint16_t;
	using boost::uint32_t;
#else
	//We have a decent compiler - use real C++0x features
	#include <cstdint>
	#include <functional>
	#include <memory>
	#define polyvox_shared_ptr std::shared_ptr
	#define polyvox_function std::function
	#define polyvox_bind std::bind
	#define polyvox_placeholder_1 std::placeholders::_1
	#define polyvox_placeholder_2 std::placeholders::_2
	#define polyvox_hash std::hash
	//#define static_assert static_assert //we can use this
#endif

#endif
