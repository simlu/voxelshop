package com.vitco.util.lang;

/*
* Interface
*
* Manages languages. We can set the language and country and the language bundle to be used.
* A value for a key is obtained with getString. Init must be called when everything is set
* correctly.
*/

public interface LangSelectorInterface {
    void setLanguageBundle(String filename);
    void setLanguage(String language);
    void setCountry(String country);
    String getString(String key);
    void init();
}
