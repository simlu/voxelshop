#include "IwUI.h"
#include "ui.h"

CIwUIElement* g_Screen;

void ui_load() {
	IwGetResManager()->LoadGroup("main_menu.group");

	//Set the default style sheet
	CIwResource* pResource = IwGetResManager()->GetResNamed("iwui", IW_UI_RESTYPE_STYLESHEET);
	IwGetUIStyleManager()->SetStylesheet(IwSafeCast<CIwUIStylesheet*>(pResource));
}

void ui_set_screen(const char* screen) {
    //Find the dialog template
    CIwUIElement* pDialogTemplate = (CIwUIElement*)IwGetResManager()->GetResNamed(screen, "CIwUIElement");

    if (!pDialogTemplate)
        return;

    //Remove the old screen
    if (g_Screen)
        IwGetUIView()->DestroyElements();

    //And instantiate it
    g_Screen = pDialogTemplate->Clone();
    IwGetUIView()->AddElement(g_Screen);
    IwGetUIView()->AddElementToLayout(g_Screen);
}