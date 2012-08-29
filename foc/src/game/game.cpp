#include "IwGx.h"
#include "game.h"
#include "states/init_state.h"
#include <utils/ssm.h>
#include <sstream>

game::game()
:timePerFrame(1000 / 60) // 1000 / 60 = 60 fps
{
	currentTime = s3eTimerGetMs();
	fixedTimestepAccumulator = 0;
}

void game::run() {
	// Initialise the IwGx drawing module
	IwGxInit();	

	// tell all states about us
	_sm.set_parent(this);

	// enter the intial state
	_sm.switch_state(new init_state());

	while(1) {
		this->update();
		this->draw();

        // Sleep for 0ms to allow the OS to process events etc.
        s3eDeviceYield(0);

		 // die on request to
		 if(s3eDeviceCheckQuitRequest()) {
			 break;
		 }
	}
}

void game::update() {
	// now
	uint64 newTime = s3eTimerGetMs();
	// diff between now and the last currentTime update
	// essentially the length of the last frame
	uint64 dt = newTime - currentTime;
	// accumulate the time spent
	fixedTimestepAccumulator += dt;
	// execute frames of length dt
	while(fixedTimestepAccumulator >= dt) {
		// ALL UPDATE CODE GOES HERE 
		_sm.update(dt);
		// move on to next one
		fixedTimestepAccumulator -= dt;
	}
}

void game::draw() {
	// Set the background colour to (opaque) blue
	IwGxSetColClear(0, 0, 0xff, 0xff);

	// Clear the surface
	IwGxClear();

	/*
	std::stringstream tmp;
	tmp << frameTime;


	//IwGxPrintString(120, 150, tmp.str().c_str());
	IwTrace(DEFAULT, (tmp.str().c_str()));
	*/

	// Standard EGL-style flush of drawing to the surface
	IwGxFlush();

	// Standard EGL-style flipping of double-buffers
	IwGxSwapBuffers();
}

game::~game() {

}
