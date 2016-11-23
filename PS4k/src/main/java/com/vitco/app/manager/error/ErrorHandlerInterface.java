package com.vitco.app.manager.error;

import com.vitco.app.layout.content.console.ConsoleInterface;
import com.vitco.app.manager.lang.LangSelectorInterface;

/**
 * Deals with all the exceptions in the program. Writes them to file and tries to upload
 * them to a server.
 */
public interface ErrorHandlerInterface extends Thread.UncaughtExceptionHandler {
    void handle(Throwable e);
    void setLangSelector(LangSelectorInterface langSelector);
    void setDebugReportUrl(String debugReportUrl);
    void setConsole(ConsoleInterface console);
}
