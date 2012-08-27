#ifndef FOS_SOCKET_H
#define FOS_SOCKET_H

#include <boost/algorithm/string.hpp>
using namespace std;
using namespace boost;

struct s3eSocket; 

class fos_socket {
private:
	s3eSocket		*_socket;
	bool			_is_connected;
public:
	fos_socket();

	void connect(string ip, uint16 port);

	bool is_connected() { return _is_connected; }

private:
	void connection_succeeded();
	void connection_failed();

	friend int32 connect_callback(s3eSocket *s, void *systemData, void *userData);
};

int32 connect_callback(s3eSocket *s, void *systemData, void *userData);

#endif