#ifndef MENU_HANDLERS_H
#define MENU_HANDLERS_H

#include "IwUI.h"

class main_menu_handler : public IIwUIEventHandler {
private:
	CIwString<32> _cur_click;
public:
	CIwString<32>& get_cur_click() {
		return _cur_click;
	}

	void clear_cur_click() {
		_cur_click.setLength(0);
	}

    virtual bool FilterEvent(CIwEvent* pEvent)
    {
        if (pEvent->GetID() == IWUI_EVENT_BUTTON )
        {
            CIwPropertyString uilink;
            if ((IwSafeCast<CIwUIElement*>(pEvent->GetSender()))->GetProperty("uilink", uilink, true) )
            {
                if (!strcmp(uilink.c_str(), "exit") )
                {
                    s3eDeviceRequestQuit();
                    return true;
                }

                _cur_click = uilink.c_str();

                return true;
            }
        }

        return false;
    }

    virtual bool HandleEvent(CIwEvent* pEvent) {
        return false;
    }
};

#endif