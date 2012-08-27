#include "s3eSocket.h"
#include "fos_socket.h"
#include <string.h>

fos_socket::fos_socket() {
	_is_connected = false;
}

void fos_socket::connect(string ip, uint16 port) {
	// create a socket
	_socket = s3eSocketCreate(S3E_SOCKET_TCP, 0);
	if(_socket == NULL) {
		// um wtf do we do now?
	}

	// setup the connection
    s3eInetAddress addr;
    memset(&addr, 0, sizeof(addr));
	strcpy(addr.m_String, ip.c_str());
	s3eInetAton(&addr.m_IPAddress, ip.c_str());
	addr.m_Port = s3eInetHtons(port);

	// attempt connection
	s3eSocketConnect(_socket, &addr, connect_callback, this);
}

void fos_socket::connection_succeeded() {
	// NEED TO TRIGGER STATE CHANGE
	_is_connected = true;
}

void fos_socket::connection_failed() {
	// NEED TO TRIGGER STATE CHANGE
	_is_connected = false;
	
	// HOW DO WE STOP AND CLEANUP?
	//s3eSocketClose(_socket);
}

int32 connect_callback(s3eSocket *s, void *systemData, void *userData) {
	s3eResult res = *(s3eResult*)systemData;
	fos_socket *socket = (fos_socket*)userData;

	if (res == S3E_RESULT_SUCCESS) {
		socket->connection_succeeded();
	} else {
		socket->connection_failed();
	}

	return 0;
}