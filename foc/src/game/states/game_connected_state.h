#ifndef GAME_CONNECTED_STATE_H
#define GAME_CONNECTED_STATE_H

#include <utils\ssm.h>
#include <game\game.h>
#include <network\fos_socket.h>
#include "game_disconnected_state.h"

class connected_state : public simple_state {
private:
	shared_fos_socket _shared_socket;
public:
	connected_state(shared_fos_socket shared_socket) {
		_shared_socket = shared_socket;

		// send the login message
		/*fantasy_message fm;
		fm.set_type(LOGIN);
		fm.mutable__login()->set_auth(66);
		_shared_socket->send(fm);*/
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
		}
	}
	void on_exit() {
	}
};

#endif