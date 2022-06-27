package com.redhat.acs.exporter;

import java.util.ResourceBundle;

public class ApplicationProperties {
    private static ApplicationProperties INSTANCE;

    private ResourceBundle bundle = ResourceBundle.getBundle("application");

    private ApplicationProperties() { }

    public static ApplicationProperties getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new ApplicationProperties();
        }
        return INSTANCE;
    }

    public String get(String key) {
        return bundle.getString(key);
    }
}
