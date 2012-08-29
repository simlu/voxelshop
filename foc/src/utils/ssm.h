#ifndef SSM_H
#define SSM_H

#include <s3eTypes.h>
#include <boost/shared_ptr.hpp>

class ssm;

class simple_state {
protected:
	ssm &_sm;
public:
	simple_state(ssm &sm):_sm(sm) { }
	virtual void on_enter() = 0;
	virtual void update(uint64 dt) = 0;
	virtual void on_exit() = 0;
};

typedef boost::shared_ptr<simple_state> shared_simple_state;

class ssm {
private:
	shared_simple_state _cur_state;

public:
	void switch_state(shared_simple_state new_state);
	void update(uint64 dt);
};

#endif