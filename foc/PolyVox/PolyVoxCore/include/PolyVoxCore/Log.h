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

#ifndef __PolyVox_Log_H__
#define __PolyVox_Log_H__

#include "PolyVoxImpl/TypeDef.h"

#include <string>

//Note: The functions in this file are not for the user to call - they are 
//intended for internal use only. The only exception is that you may set the
//logHandler pointer to point at your own handling funtion for printing, etc.

namespace PolyVox
{
	////////////////////////////////////////////////////////////////////////////////
	/// Log levels for filtering logging events
	////////////////////////////////////////////////////////////////////////////////
	enum LogSeverity
	{
		LS_DEBUG, ///< Only displayed if it is a debug build
		LS_INFO,
		LS_WARN,
		LS_ERROR
	};	

	POLYVOX_API extern void (*logHandler)(std::string, int severity);
}

//Debug severity messages are only used if we are a debug build
#ifdef _DEBUG
	#define POLYVOX_LOG_DEBUG(message) if(logHandler){logHandler(message, LS_DEBUG);}
#else
	#define POLYVOX_LOG_DEBUG(message)
#endif

//Other severity levels work in both debug and release
#define POLYVOX_LOG_INFO(message) if(logHandler){logHandler(message, LS_INFO);}
#define POLYVOX_LOG_WARN(message) if(logHandler){logHandler(message, LS_WARN);}
#define POLYVOX_LOG_ERROR(message) if(logHandler){logHandler(message, LS_ERROR);}

#endif
