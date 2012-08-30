#include "fos_socket.h"
#include "fantasy_messages.pb.h"
#include <string.h>
#include <IwDebug.h>
#include <s3eDevice.h>
#include <google/protobuf/io/coded_stream.h>

using namespace google::protobuf::io;

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

void fos_socket::receive() {
	// make sure we're connected
	if(_is_connected) {
		//int32 ret = s3eSocketRecv(_socket, _read_buf, _read_buf_len, 0);
	}
}

void fos_socket::send(fantasy_message fm) {
	// prepend the length as a varint32
	int32 totalMsgLen = fm.ByteSize();
	uint8* _send_buf_end = CodedOutputStream::WriteVarint32ToArray(totalMsgLen, _send_buf);
	int32 len_change = _send_buf_end - _send_buf;
	totalMsgLen += len_change;

	// try to serialize to our byte buffer
	if(fm.SerializePartialToArray(_send_buf_end, _send_buf_len - len_change)) {

		int32 msgLen		= totalMsgLen;
		int32 msgSent		= 0;

		// loop until all is sent
		do {
			int32 ret = s3eSocketSend(_socket, (char*)(_send_buf + msgSent), totalMsgLen - msgSent, 0);

			IwAssert(DEFAULT, ret != 0);

			// success!
			if (ret > 0) {
				msgSent += ret;
			}

			// oh noes!
			if(ret < 0) {
				// This error is OK, since S3E_SOCKET_ERR_AGAIN means, that
				// a function is in process right now
				s3eSocketError errors = s3eSocketGetError();
				if (errors == S3E_SOCKET_ERR_AGAIN) {
					// REALLY DON'T WANT TO DO THIS!
					s3eDeviceYield(50);
					// ALERT ALERT BAD BAD BAD
					continue;
				}
			}
		} while(msgSent < msgLen);
	} else {
		// FAIL WHAT TO DO!?
	}
}

fos_socket::~fos_socket() {
	if(_socket != NULL) {
		s3eSocketClose(_socket);
		_socket = NULL;
	}
}