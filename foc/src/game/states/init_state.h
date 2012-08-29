#ifndef INIT_STATE_H
#define INIT_STATE_H

#include <utils/ssm.h>

class init_state : public simple_state {
	public:
	init_state(ssm& sm):simple_state(sm){}

	void on_enter() {
		
	}
	void update(uint64 dt) {
		
		
		//_sm.switch_state(shared_simple_state(new init_state(_sm)));
	}
	void on_exit() {
	}
};

#endif