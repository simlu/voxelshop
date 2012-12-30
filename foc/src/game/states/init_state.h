#ifndef INIT_STATE_H
#define INIT_STATE_H

#include "utils/ssm.h"
#include "auth_state.h"

class init_state : public simple_state {
	public:

	void on_enter() {
	}
	void update(uint64 dt) {
		// start in the auth state
		_sm->switch_state(new auth_state());
	}
	void on_exit() {
	}
};

#endif