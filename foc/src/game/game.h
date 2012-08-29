#ifndef GAME_H
#define GAME_H

#include <s3eTypes.h>
#include <utils\ssm.h>

class game {
public:
	game();
	void run();
	~game();
private:
	ssm _sm;

    const uint64 timePerFrame;
	uint64 currentTime;
	uint64 fixedTimestepAccumulator;
	void update();

	void draw();
};

#endif