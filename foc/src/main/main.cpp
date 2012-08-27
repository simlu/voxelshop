#include <game/game.h>
#include <boost/scoped_ptr.hpp>
using namespace std;
using namespace boost;

int main() {
	scoped_ptr<game> g(new game);

	g->run();

    return 0;
}
