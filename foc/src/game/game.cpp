#include "IwGx.h"
#include "game.h"

#include <network\fos_socket.h>

void game::init() {
	// Initialise the IwGx drawing module
    IwGxInit();

    // Set the background colour to (opaque) blue
    IwGxSetColClear(0, 0, 0xff, 0xff);
}

void game::run() {
	init();

	fos_socket sock;
	sock.connect("10.0.1.10", 8080);
	

    // Loop forever, until the user or the OS performs some action to quit the app
    while (!s3eDeviceCheckQuitRequest())
    {
        // Clear the surface
        IwGxClear();

        // Use the built-in font to display a string at coordinate (120, 150)
        //IwGxPrintString(120, 150, "Hello, World!");

		if(sock.is_connected()) {
			IwGxPrintString(120, 150, "Connected!");
		}

        // Standard EGL-style flush of drawing to the surface
        IwGxFlush();

        // Standard EGL-style flipping of double-buffers
        IwGxSwapBuffers();

        // Sleep for 0ms to allow the OS to process events etc.
        s3eDeviceYield(0);
    }

    // Shut down the IwGx drawing module
    IwGxTerminate();
}
