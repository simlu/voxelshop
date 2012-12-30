#ifndef AUTH_STATE_H
#define AUTH_STATE_H

#include "utils/ssm.h"
#include "utils/ui.h"

class auth_state : public simple_state {
	public:

	void on_enter() {
		// check for saved login credentials

		// otherwise show the login screen
		// NEED TO SET CALLBACK AND PASS IT THIS STATE SO IT CAN DO STUFF
		ui_set_screen("login");
	}
	void update(uint64 dt) {		
	}
	void on_exit() {
	}
};

#endif