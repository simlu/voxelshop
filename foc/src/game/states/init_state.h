#ifndef INIT_STATE_H
#define INIT_STATE_H

#include <utils/ssm.h>
#include "connecting_state.h"

class init_state : public simple_state {
	public:

	void on_enter() {	
	}
	void update(uint64 dt) {

		// move to connecting
		_sm->switch_state(new connecting_state());
	}
	void on_exit() {
	}
};

#endif