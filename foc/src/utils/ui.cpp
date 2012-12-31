#include "IwUI.h"
#include "ui.h"

CIwUIElement* g_screen = NULL;
IIwUIEventHandler* g_event_handler = NULL;

void ui_load() {
	// set the OS keyboard
	IwGetUITextInput()->SetEditorMode(CIwUITextInput::eInlineKeyboard);

	// load our group
	IwGetResManager()->LoadGroup("main_menu.group");

	//Set the default style sheet
	CIwResource* pResource = IwGetResManager()->GetResNamed("iwui", IW_UI_RESTYPE_STYLESHEET);
	IwGetUIStyleManager()->SetStylesheet(IwSafeCast<CIwUIStylesheet*>(pResource));
}

void ui_set_screen(const char* screen, IIwUIEventHandler* event_handler) {
    //Find the dialog template
    CIwUIElement* pDialogTemplate = (CIwUIElement*)IwGetResManager()->GetResNamed(screen, "CIwUIElement");

    if (!pDialogTemplate)
        return;

	// remove the old event handler
	if(g_event_handler)
		IwGetUIController()->RemoveEventHandler(g_event_handler);

	// add the new event handler
	g_event_handler = event_handler;
	IwGetUIController()->AddEventHandler(g_event_handler);

    //Remove the old screen
    if (g_screen)
        IwGetUIView()->DestroyElements();

    //And instantiate it
    g_screen = pDialogTemplate->Clone();
    IwGetUIView()->AddElement(g_screen);
    IwGetUIView()->AddElementToLayout(g_screen);
}

void ui_clear_screen() {
	if(g_event_handler)
		IwGetUIController()->RemoveEventHandler(g_event_handler);

    if (g_screen)
        IwGetUIView()->DestroyElements();
}