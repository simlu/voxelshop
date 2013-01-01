#ifndef INIT_STATE_H
#define INIT_STATE_H

#include "utils/ssm.h"
#include "login_connecting_state.h"

class init_state : public simple_state {
	public:

	void on_enter() {
	}
	void update(uint64 dt) {
		// start in the login connecting state
		_sm->switch_state(new login_connecting_state());
	}
	void on_exit() {
	}
};

#endif