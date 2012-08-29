#include "ssm.h"

void ssm::switch_state(shared_simple_state new_state) {
	if(_cur_state != NULL) {
		_cur_state->on_exit();
	}
	_cur_state = new_state;
	_cur_state->on_enter();
}

void ssm::update(uint64 dt) {
	if(_cur_state != NULL) {
		_cur_state->update(dt);
	}
}