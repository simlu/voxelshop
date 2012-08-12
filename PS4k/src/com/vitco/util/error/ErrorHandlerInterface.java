package com.vitco.util.error;

import com.vitco.logic.frames.console.ConsoleViewInterface;
import com.vitco.util.lang.LangSelectorInterface;

/**
 * Deals with all the exceptions in the program. Writes them to file and tries to upload
 * them to a server.
 */
public interface ErrorHandlerInterface extends Thread.UncaughtExceptionHandler {
    void handle(Throwable e);
    void setLangSelector(LangSelectorInterface langSelector);
    void setDebugReportUrl(String debugReportUrl);
    void setConsoleView(ConsoleViewInterface consoleView);
}
