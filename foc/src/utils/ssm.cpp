#include "ssm.h"

ssm::ssm() 
:_parent(NULL),_cur_state(NULL),_next_state(NULL)
{
}

void ssm::switch_state(simple_state *new_state) {
	_next_state = new_state;
}

void ssm::update(uint64 dt) {
	if(_next_state != NULL) {
		if(_cur_state != NULL) {
			_cur_state->on_exit();
			delete _cur_state;
			_cur_state = NULL;
		}
		_cur_state = _next_state;
		_next_state = NULL;
		_cur_state->_sm = this;
		_cur_state->_parent = this->_parent;
		_cur_state->on_enter();
	}

	if(_cur_state != NULL) {
		_cur_state->update(dt);
	}
}

ssm::~ssm() {
	if(_next_state != NULL) {
		_next_state->on_exit();
		delete _next_state;
		_next_state = NULL;
	}
	if(_cur_state != NULL) {
		_cur_state->on_exit();
		delete _cur_state;
		_cur_state = NULL;
	}
}