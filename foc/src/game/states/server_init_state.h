#ifndef SERVER_INIT_STATE_H
#define SERVER_INIT_STATE_H

#include <utils\ssm.h>
#include <game\game.h>
#include <network\fos_socket.h>
#include <network\fantasy_messages.pb.h>
using namespace com::pixelatedgames::fos::protobufs;

class server_init_state : public simple_state {
private:
	shared_fos_socket _shared_socket;
public:
	server_init_state(shared_fos_socket shared_socket) {
		_shared_socket = shared_socket;

		// send the login message
		fantasy_message fm;
		fm.set_type(LOGIN);
		fm.mutable__login()->set_authentication(66);

		_shared_socket->send(fm);
	}
	void on_enter() {
	}
	void update(uint64 dt) {
	}
	void on_exit() {
	}
};

#endif