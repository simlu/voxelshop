#include "s3eSocket.h"
#include "fos_socket.h"
#include <string.h>

fos_socket::fos_socket() {
	_socket = NULL;
	_is_connected = false;
	_errors = S3E_SOCKET_ERR_NONE;
}

void fos_socket::connect(std::string ip, uint16 port) {
	// create a socket
	_socket = s3eSocketCreate(S3E_SOCKET_TCP, 0);
	if(_socket == NULL) {
		_errors = s3eSocketGetError();
		return;
	}

	// set options
	int on = 1;
	s3eSocketSetOpt(_socket, S3E_SOCKET_NODELAY, &on, sizeof(on));

	// setup the connection
    s3eInetAddress addr;
    memset(&addr, 0, sizeof(addr));
	strcpy(addr.m_String, ip.c_str());
	s3eInetAton(&addr.m_IPAddress, ip.c_str());
	addr.m_Port = s3eInetHtons(port);

	// attempt connection
	if(s3eSocketConnect(_socket, &addr, connect_callback, this) != S3E_RESULT_SUCCESS) {
		s3eSocketError errors = s3eSocketGetError();
		switch (errors) {
            // These errors are 'OK', because they mean,
            // that a connect is in progress
            case S3E_SOCKET_ERR_INPROGRESS:
            case S3E_SOCKET_ERR_ALREADY:
            case S3E_SOCKET_ERR_WOULDBLOCK:
				break;
			default:
				// A 'real' error happened
				_errors = errors;
				return;
		}
	}
}

void fos_socket::connection_succeeded() {
	// change flag
	_is_connected = true;
}

void fos_socket::connection_failed() {
	// change flag
	_is_connected = false;

	// save errors
	_errors = s3eSocketGetError();
	
	// kill it
	s3eSocketClose(_socket);
	_socket = NULL;
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

fos_socket::~fos_socket() {
	if(_socket != NULL) {
		s3eSocketClose(_socket);
		_socket = NULL;
	}
}