# Copyright (c) 2008-2012 Matt Williams
# Copyright (c) 2008-2012 David Williams
# 
# This software is provided 'as-is', without any express or implied
# warranty. In no event will the authors be held liable for any damages
# arising from the use of this software.
# 
# Permission is granted to anyone to use this software for any purpose,
# including commercial applications, and to alter it and redistribute it
# freely, subject to the following restrictions:
# 
# 1. The origin of this software must not be misrepresented; you must not
# claim that you wrote the original software. If you use this software
# in a product, an acknowledgment in the product documentation would be
# appreciated but is not required.
# 
# 2. Altered source versions must be plainly marked as such, and must not be
# misrepresented as being the original software.
# 
# 3. This notice may not be removed or altered from any source
# distribution.

CMAKE_MINIMUM_REQUIRED(VERSION 2.6)

PROJECT(PolyVoxUtil)

#Projects source files
SET(UTIL_SRC_FILES
	source/Dummy.cpp
)

#Projects headers files
SET(UTIL_INC_FILES
	include/PolyVoxUtil/Serialization.h
	include/PolyVoxUtil/Serialization.inl
	include/PolyVoxUtil/VolumeChangeTracker.h	
	include/PolyVoxUtil/VolumeChangeTracker.inl
)

ADD_DEFINITIONS(-DPOLYVOX_SHARED_EXPORTS) #Export symbols in the .dll

#"Sources" and "Headers" are the group names in Visual Studio.
#They may have other uses too...
SOURCE_GROUP("Sources" FILES ${UTIL_SRC_FILES})
SOURCE_GROUP("Headers" FILES ${UTIL_INC_FILES})

#Tell CMake the paths
INCLUDE_DIRECTORIES(${CMAKE_CURRENT_SOURCE_DIR}/include ${PolyVoxCore_SOURCE_DIR}/include)
#There has to be a better way!
LINK_DIRECTORIES(${PolyVoxCore_BINARY_DIR}/debug ${PolyVoxCore_BINARY_DIR}/release ${PolyVoxCore_BINARY_DIR})

#Util
#Build
IF(LIBRARY_TYPE STREQUAL "STATIC")
	ADD_LIBRARY(PolyVoxUtil STATIC ${UTIL_SRC_FILES} ${UTIL_INC_FILES})
	IF(UNIX)
		SET_TARGET_PROPERTIES(PolyVoxCore PROPERTIES COMPILE_FLAGS -fPIC)
	ENDIF()
ENDIF()
IF(LIBRARY_TYPE STREQUAL "DYNAMIC")
	ADD_LIBRARY(PolyVoxUtil SHARED ${UTIL_SRC_FILES} ${UTIL_INC_FILES})
	SET_TARGET_PROPERTIES(PolyVoxUtil PROPERTIES COMPILE_FLAGS "-DPOLYVOX_SHARED_EXPORTS")
ENDIF()
SET_PROPERTY(TARGET PolyVoxUtil PROPERTY FOLDER "library/PolyVoxUtil")

TARGET_LINK_LIBRARIES(PolyVoxUtil PolyVoxCore)
SET_TARGET_PROPERTIES(PolyVoxUtil PROPERTIES VERSION ${POLYVOX_VERSION} SOVERSION ${POLYVOX_VERSION_MAJOR})
IF(MSVC)
		SET_TARGET_PROPERTIES(PolyVoxUtil PROPERTIES COMPILE_FLAGS "/W4 /wd4251 /wd4127") #Disable warning on STL exports
ENDIF(MSVC)

#Install
IF(WIN32)
	INSTALL(TARGETS PolyVoxUtil
		RUNTIME DESTINATION PolyVoxUtil/bin COMPONENT library
		LIBRARY DESTINATION PolyVoxUtil/lib COMPONENT library
		ARCHIVE DESTINATION PolyVoxUtil/lib COMPONENT library
	)

	#Install the util header files.
	INSTALL(DIRECTORY include DESTINATION PolyVoxUtil COMPONENT development PATTERN "*.svn*" EXCLUDE)
	
	#On windows, we also install the debug information. It's unfortunate that we have to hard-code
	#the 'Debug' part of the path, but CMake doesn't seem to provide a way around this. The best I
	#found was: http://www.cmake.org/pipermail/cmake/2007-October/016924.html (and it is a bit ugly).
	INSTALL(FILES ${CMAKE_CURRENT_BINARY_DIR}/Debug/PolyVoxUtil.pdb DESTINATION PolyVoxUtil/lib CONFIGURATIONS Debug)
	INSTALL(FILES ${CMAKE_CURRENT_BINARY_DIR}/RelWithDebInfo/PolyVoxUtil.pdb DESTINATION PolyVoxUtil/lib CONFIGURATIONS RelWithDebInfo)
ELSE(WIN32)
	INSTALL(TARGETS PolyVoxUtil
		RUNTIME DESTINATION bin COMPONENT library
		LIBRARY DESTINATION lib COMPONENT library
		ARCHIVE DESTINATION lib COMPONENT library
	)

	#Install the util header files.
	INSTALL(DIRECTORY include/ DESTINATION include/PolyVoxUtil COMPONENT development PATTERN "*.svn*" EXCLUDE)
ENDIF(WIN32)
