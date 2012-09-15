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

PROJECT(PolyVox)

add_subdirectory(PolyVoxCore)
add_subdirectory(PolyVoxUtil)
add_subdirectory(bindings)

#Set up install paths e.g. for PolyVoxConfig.cmake
if(WIN32)
	set(CONFIG_FILE_DIR "CMake")
	set(PolyVoxCore_LIBRARY_INSTALL_DIRS "PolyVoxCore/lib")
	set(PolyVoxUtil_LIBRARY_INSTALL_DIRS "PolyVoxUtil/lib")
	set(PolyVoxCore_INCLUDE_INSTALL_DIRS "PolyVoxCore/include")
	set(PolyVoxUtil_INCLUDE_INSTALL_DIRS "PolyVoxUtil/include")
	set(PolyVox_DOC_INSTALL_DIR "PolyVox/doc")
else(WIN32)
	set(CONFIG_FILE_DIR "share/PolyVox/cmake")
	set(PolyVoxCore_LIBRARY_INSTALL_DIRS "lib")
	set(PolyVoxUtil_LIBRARY_INSTALL_DIRS "lib")
	set(PolyVoxCore_INCLUDE_INSTALL_DIRS "include/PolyVoxCore")
	set(PolyVoxUtil_INCLUDE_INSTALL_DIRS "include/PolyVoxUtil")
	set(PolyVox_DOC_INSTALL_DIR "share/doc/packages/polyvox")
endif(WIN32)

configure_file(${CMAKE_CURRENT_SOURCE_DIR}/PolyVoxConfig.cmake.in ${CMAKE_CURRENT_BINARY_DIR}/PolyVoxConfig.cmake @ONLY)
install(FILES ${CMAKE_CURRENT_BINARY_DIR}/PolyVoxConfig.cmake DESTINATION ${CONFIG_FILE_DIR} COMPONENT development)

if(DOXYGEN_FOUND)
	#configure_file(${CMAKE_CURRENT_SOURCE_DIR}/polyvox.css ${CMAKE_CURRENT_BINARY_DIR}/polyvox.css)
	configure_file(${CMAKE_CURRENT_SOURCE_DIR}/Doxyfile.in ${CMAKE_CURRENT_BINARY_DIR}/Doxyfile)
	#This is just the default doc target which only runs Doxygen
	add_custom_target(doc
		COMMAND ${DOXYGEN_EXECUTABLE}
		WORKING_DIRECTORY ${CMAKE_CURRENT_BINARY_DIR}
		COMMENT "Building documentation"
		SOURCES Doxyfile.in polyvox.qhcp.in Mainpage.dox
		VERBATIM
	)
	set_target_properties(doc PROPERTIES PROJECT_LABEL "Documentation") #Set label seen in IDE
	set_property(TARGET doc PROPERTY FOLDER "library/doc")
	
	#If we found qcollectiongenerator then do more processing
	if(QT_QCOLLECTIONGENERATOR_EXECUTABLE)
		configure_file(${CMAKE_CURRENT_SOURCE_DIR}/polyvox.qhcp.in ${CMAKE_CURRENT_BINARY_DIR}/doc/qthelp/polyvox.qhcp) #The QtHelp config file
		
		#We attach this command to the doc target so it will be run automatically
		add_custom_command(TARGET doc POST_BUILD
			COMMAND ${QT_QCOLLECTIONGENERATOR_EXECUTABLE} polyvox.qhcp -o polyvox.qhc
			WORKING_DIRECTORY ${CMAKE_CURRENT_BINARY_DIR}/doc/qthelp
			COMMENT "Compiling API documentation to Qt Help format"
		)
		
		install(FILES
			${CMAKE_CURRENT_BINARY_DIR}/doc/qthelp/polyvox.qhc
			${CMAKE_CURRENT_BINARY_DIR}/doc/qthelp/polyvox.qch
			DESTINATION ${PolyVox_DOC_INSTALL_DIR}/qthelp
			COMPONENT development
			OPTIONAL
		)
	endif()
endif()

#add_subdirectory(bindings)
