package com.vitco.util;

/*
* Interface
*
* Manages languages. We can set the language and country and the language bundle to be used.
* A value for a key is obtained with getString. Init must be called when everything is set
* correctly.
*/

public interface LangSelectorInterface {
    public void setLanguageBundle(String filename);
    public void setLanguage(String language);
    public void setCountry(String country);
    public String getString(String key);
    public void init();
}
