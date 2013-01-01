#ifndef LOGIN_DISCONNECTED_STATE_H
#define LOGIN_DISCONNECTED_STATE_H

#include "utils\ssm.h"
#include "network\fos_socket.h"

class login_disconnected_state : public simple_state {
public:
	login_disconnected_state(s3eSocketError errors) {
	}

	void on_enter() {
	}
	void update(uint64 dt) {
		// wait for a response from the user
		// do they want to try to connect again?
	}
	void on_exit() {
	}
};

#endif