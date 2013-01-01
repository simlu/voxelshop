#ifndef LOGIN_CONNECTED_STATE_H
#define LOGIN_CONNECTED_STATE_H

#include "utils\ssm.h"
#include "network\fos_socket.h"
#include "login_disconnected_state.h"

class login_connected_state : public simple_state {
private:
	shared_fos_socket _shared_socket;
public:
	login_connected_state(shared_fos_socket shared_socket) {
		_shared_socket = shared_socket;
	}
	void on_enter() {
	}
	void update(uint64 dt) {
		// check for errors
		if(_shared_socket->is_errors()) {
			// move to disconnected
			_sm->switch_state(new login_disconnected_state(_shared_socket->is_errors()));
		} else {
			// always ping
			_shared_socket->ping();

			// receieve all data
			_shared_socket->receive();
		}
	}
	void on_exit() {
	}
};

#endif