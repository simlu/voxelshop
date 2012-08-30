#include "IwGx.h"
#include <game\game.h>

int main() {
	{
		game g;
		g.run();
	}
	
	// die
	IwGxTerminate();
    return 0;
}
