package com.vitco.util.lang;

import javax.annotation.PostConstruct;
import java.util.Locale;
import java.util.ResourceBundle;

/*
* Implementation of LangSelectorInterface
*
* Manages languages. We can set the language and country and the language bundle to be used.
* A value for a key is obtained with getString. Init must be called when everything is set
* correctly.
*/

public final class LangSelector implements LangSelectorInterface {
    protected String language;
    protected String country;
    protected Locale locale;
    protected ResourceBundle rb;
    protected String defaultFile;

    @PostConstruct
    @Override
    public void init() {
        locale = new Locale(language, country);
        rb = ResourceBundle.getBundle(defaultFile, locale);
    }

    @Override
    public void setLanguageBundle(String filename) {
        this.defaultFile = filename;
    }

    @Override
    public void setLanguage(String language) {
        this.language = language;
    }

    @Override
    public void setCountry(String country) {
        this.country = country;
    }

    @Override
    public String getString(String key) {
        return rb.getString(key);
    }
}
