#ifndef CONNECTED_STATE_H
#define CONNECTED_STATE_H

#include <utils\ssm.h>
#include <game\game.h>
#include <network\fos_socket.h>
#include "disconnected_state.h"
#include "server_init_state.h"

class connected_state : public simple_state {
private:
	shared_fos_socket _shared_socket;
	ssm sm;
public:
	connected_state(shared_fos_socket shared_socket) {
		_shared_socket = shared_socket;

		sm.switch_state(new server_init_state(_shared_socket));
	}
	void on_enter() {
	}
	void update(uint64 dt) {
		// check for errors
		if(_shared_socket->is_errors()) {
			// move to disconnected
			_sm->switch_state(new disconnected_state(_shared_socket->is_errors()));
		} else {
			// receieve all data
			_shared_socket->receive();

			// update the sub statemachine
			sm.update(dt);
		}
	}
	void on_exit() {
	}
};

#endif