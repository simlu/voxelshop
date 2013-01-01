#ifndef UI_H
#define UI_H

void ui_load();

void ui_set_handler(IIwUIEventHandler* event_handler);

void ui_set_screen(const char* screen);

#endif