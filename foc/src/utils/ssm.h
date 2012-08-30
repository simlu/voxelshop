#ifndef SSM_H
#define SSM_H

#include <s3eTypes.h>

class ssm;

class simple_state {
protected:
	void *_parent;
	ssm *_sm;
public:
	virtual ~simple_state(){}

	virtual void on_enter() = 0;
	virtual void update(uint64 dt) = 0;
	virtual void on_exit() = 0;

	friend class ssm;
};

class ssm {
private:
	void *_parent;
	simple_state *_cur_state;
	simple_state *_next_state;
public:
	ssm();
	~ssm();

	void switch_state(simple_state *new_state);
	void update(uint64 dt);

	void set_parent(void *parent) { _parent = parent; }
};

#endif