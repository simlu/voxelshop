#ifndef AUTH_STATE_H
#define AUTH_STATE_H

#include "utils/ssm.h"
#include "utils/ui.h"
#include "ui/menu_handlers.h"

class login_auth_state : public simple_state {
private:
	main_menu_handler* _handler;

public:
	login_auth_state() {
		_handler = new main_menu_handler();
	}
	~login_auth_state() {
		delete _handler;
	}

	void on_enter() {
		// check for saved login credentials

		// otherwise ask to create a new character or login
		ui_set_screen("new_or_load", _handler);
	}
	
	void update(uint64 dt) {

		// deal with menu changing
		CIwString<32>& cur_click = _handler->get_cur_click();
		if(cur_click.length()) {
			ui_set_screen(cur_click.c_str(), _handler);
			_handler->clear_cur_click();
		}
	}

	void on_exit() {
	}
};

#endif