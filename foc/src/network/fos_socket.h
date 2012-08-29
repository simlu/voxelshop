#ifndef FOS_SOCKET_H
#define FOS_SOCKET_H

#include <boost\shared_ptr.hpp>

struct s3eSocket; 
enum s3eSocketError;

class fos_socket {
private:
	s3eSocket		*_socket;
	bool			_is_connected;
	s3eSocketError	_errors;
public:
	fos_socket();
	~fos_socket();

	void connect(std::string ip, uint16 port);

	bool is_connected() { return _is_connected; }
	s3eSocketError is_errors() { return _errors; } 

private:
	void connection_succeeded();
	void connection_failed();

	friend int32 connect_callback(s3eSocket *s, void *systemData, void *userData);
};

int32 connect_callback(s3eSocket *s, void *systemData, void *userData);

typedef boost::shared_ptr<fos_socket> shared_fos_socket;

#endif